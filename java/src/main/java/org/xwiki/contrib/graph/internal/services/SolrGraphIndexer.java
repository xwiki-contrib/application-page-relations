/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xwiki.contrib.graph.internal.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.graph.XWikiGraphFactory;
import org.xwiki.contrib.graph.XWikiGraphIndexer;
import org.xwiki.contrib.graph.internal.metadata.XWikiVertexSolrMetadataExtractor;
import org.xwiki.contrib.graph.internal.model.Names;
import org.xwiki.hypergraph.three.Hyperedge;
import org.xwiki.hypergraph.GraphException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.search.solr.internal.api.FieldUtils;
import org.xwiki.search.solr.internal.api.SolrIndexer;
import org.xwiki.search.solr.internal.api.SolrIndexerException;
import org.xwiki.search.solr.internal.api.SolrInstance;
import org.xwiki.search.solr.internal.metadata.LengthSolrInputDocument;
import org.xwiki.search.solr.internal.metadata.TypedValue;
import org.xwiki.search.solr.internal.reference.SolrReferenceResolver;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
@Singleton
@Named("solr")
public class SolrGraphIndexer implements XWikiGraphIndexer
{
    /**
     * Used to differentiate the properties added dynamically by XWikiGraph to the Solr index from the standard XWiki
     * properties.
     */
    public static final String PROPERTY_GRAPH_PREFIX = "property.graph.";

    // Fields that should not rewritten when updating the index, otherwise a conflict occurs
    final static List<String> VERSION_FIELDS = Arrays.asList("version", "_version_");

    /**
     * Used to find the resolver.
     */
    @Inject
    protected ComponentManager componentManager;

    @Inject
    private Logger logger;

    @Inject
    private Provider<SolrInstance> solrInstanceProvider;

    @Inject
    private Provider<XWikiContext> contextualizer;

    @Inject
    private Provider<SolrIndexer> solr;

    @Inject
    private XWikiGraphFactory factory;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    /**
     * Copied from {@link org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor}
     */
    protected void addFieldValueOnce(SolrInputDocument solrDocument, String fieldName, Object fieldValue)
    {
        Collection<Object> fieldValues = solrDocument.getFieldValues(fieldName);
        if (fieldValues == null || !fieldValues.contains(fieldValue)) {
            solrDocument.addField(fieldName, fieldValue);
        }
    }

    protected String getFieldName(String vertexRelativeName)
    {
        return getFieldName(factory.getIdentifier(vertexRelativeName));
    }

    protected String getFieldName(DocumentReference vertex)
    {
        return FieldUtils
                .getFieldName(PROPERTY_GRAPH_PREFIX + serializer.serialize(vertex), TypedValue.STRING,
                        null);
    }

    /**
     * Adapted from {@link org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor} TODO: this should be
     * refactored to avoid dupblication. See also {@link #setDocumentFields(DocumentReference, SolrInputDocument)}
     */
    protected Locale getLocale(DocumentReference documentReference) throws SolrIndexerException
    {
        Locale locale = null;

        try {
            if (documentReference.getLocale() != null && !documentReference.getLocale().equals(Locale.ROOT)) {
                locale = documentReference.getLocale();
            } else {
                XWikiContext xcontext = this.contextualizer.get();
                locale = xcontext.getWiki().getDocument(documentReference, xcontext).getRealLocale();
            }
        } catch (Exception e) {
            throw new SolrIndexerException(
                    String.format("Exception while fetching the locale of the document '%s'", documentReference), e);
        }

        // This is a hack to make sure that the locale is never empty. It can be empty in case the method is called
        // on a reference of a document that does not exist yet during a refactoring job where an update of edges
        // destinations occurs before the destination document has been entirely created. Since edges are meant
        // to relate to content pages, all their origins and destinations are supposed to have a locale. If no
        // locale is found, use the default one of the current wiki.
        // TODO: handle this properly
        if (locale == null || StringUtils.isEmpty(locale.toString())) {
            XWikiContext xcontext = this.contextualizer.get();
            locale = xcontext.getWiki().getDefaultLocale(xcontext);
        }

        return locale;
    }

    protected SolrDocument getSolrDocument(EntityReference reference) throws GraphException
    {
        try {
            String id = getSolrIdentifier(reference);
            return ((GraphEmbeddedSolrInstance) solrInstanceProvider.get()).getSolrDocument(id);
        } catch (IOException | SolrServerException | SolrIndexerException e) {
            throw new GraphException(e);
        }
    }

    /**
     * Computes the Solr identifier making sure that the id always contain a locale at the end. An id without a locale
     * can be returned when the looked up reference does not exist yet in the wiki, typically when the method is called
     * when updating an edge destination during a refactoring job, while the new destination page has not been entirely
     * created yet. Since edges are meant to connect content pages, it makes no sense to have a page Solr id without a
     * locale and this can lead to errors. See also {@link #getLocale(DocumentReference)}. TODO: handle this properly.
     */
    public String getSolrIdentifier(EntityReference reference) throws SolrIndexerException
    {
        String id = getSolrReferenceResolver(reference).getId(reference);

        if (id.endsWith("_")) {
            XWikiContext context = contextualizer.get();
            id = id + context.getWiki().getDefaultLocale(context);
        }
        return id;
    }

    /**
     * Returns an initialized SolrInputDocument, initialized either with all properties as they are in the index, or
     * only the graph properties, if second parameter is false. In all cases, the standard document fields (ID, TYPE,
     * SPACE, WIKI, etc.) are initialized.
     */
    public LengthSolrInputDocument getSolrInputDocument(EntityReference reference,
            boolean withAllPropertiesFromIndex) throws GraphException
    {
        LengthSolrInputDocument solrInputDocument = new LengthSolrInputDocument();
        SolrDocument solrDocument = getSolrDocument(reference);
        // Variable solrDocument can be null if the entity has not been indexed yet.
        try {
            // We copy all existing field/value pairs starting with the prefix 'property.graph'
            // to the returned documents since this method is meant to return a SolrInputDocument as it is in the index
            // at the time of calling.
            if (solrDocument != null) {
                // Always skip the "_version_" field, because it used internally only by Solr and keeping it can raise
                // conflict issues.
                for (String fieldName : solrDocument.getFieldNames()) {
                    if (withAllPropertiesFromIndex && !VERSION_FIELDS.contains(fieldName)) {
                        Object fieldValue = solrDocument.getFieldValue(fieldName);
                        solrInputDocument.setField(fieldName, fieldValue);
                    } else if (fieldName.startsWith(PROPERTY_GRAPH_PREFIX)) {
                        // Only the property graph properties are initialized, the other ones will be added
                        // by the caller.
                        Object fieldValue = solrDocument.getFieldValue(fieldName);
                        solrInputDocument.setField(fieldName, fieldValue);
                    }
                }
            }
            // TODO: check if it's really needed, probably yes
            String id = getSolrIdentifier(reference);
            solrInputDocument.setField(FieldUtils.ID, id);
            setDocumentFields(new DocumentReference(reference.extractReference(EntityType.DOCUMENT)),
                    solrInputDocument);
            // TODO: check why FieldUtils.TYPE is not set in setDocumentFields directly
            solrInputDocument.setField(FieldUtils.TYPE, reference.getType().name());
        } catch (Exception e) {
            logger.error("Get SolrInputDocument {}", reference, e);
            throw new GraphException("getSolrInputDocument ", e);
        }
        return solrInputDocument;
    }

    /**
     * Copied from {@link org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor#
     * getResolver(EntityReference)}
     */
    protected SolrReferenceResolver getSolrReferenceResolver(EntityReference entityReference)
            throws SolrIndexerException
    {
        try {
            return this.componentManager.getInstance(SolrReferenceResolver.class,
                    entityReference.getType().getLowerCase());
        } catch (ComponentLookupException e) {
            throw new SolrIndexerException(
                    "Faile to find solr reference resolver for type reference [" + entityReference + "]");
        }
    }

    public void index(DocumentReference vertex)
    {
        logger.debug("Add vertex to index: {}", vertex);
        if (vertex != null) {
            solr.get().index(vertex, false);
            // If vertex has some edges with transitive relations, the destinations of these edges need to be indexed
            // as well, except if the destination is the Type vertex, which is special due to the "is a" relation.
            /*try {
                List<XWikiEdge> edges = getEdgesFrom(vertex);
                for (XWikiEdge edge : edges) {
                    if (!serialize(edge.getObject()).equals(serialize(getIdentifier(TYPE_VERTEX_NAME)))) {
                        XWikiRelation relation = getRelation(edge.getRelation());
                        if (edge.hasObject() && relation.isTransitive()) {
                            index(edge.getObject());
                        }
                    }
                }
            } catch (GraphException e) {
                logger.error("Error indexing {}", vertex, e);
            }*/
        }
    }

    // TODO: rename these methods since they should not be confused with the other "index" ones. These two
    //  really push the changes to Solr, while the other ones only modify a SolrDocument in memory. We need to
    //  distinguish several aspects: storage in SQL, indexing in SQL, indexing in Solr
    public void index(EntityReference reference)
    {
        logger.debug("Add reference to index: {}", reference);
        if (reference != null) {
            solr.get().index(reference, false);
        }
    }

    public void index(Hyperedge<DocumentReference> edge) throws GraphException
    {
        index(edge, true);
    }

    /**
     * Adds the value of "has-relation" directly as a property. Example: EdgeClass object with has-relation =
     * XWiki.Hypergraph.IsA and has-destination = wiki.Book will become, in Solr index: property.graph.XWiki.Hypergraph.IsA:[wiki.Book].
     */
    public void index(Hyperedge<DocumentReference> edge, boolean transitively) throws GraphException
    {
        // Index edge only if the relation is not empty.
        if (edge != null && edge.hasRelation()) {
            try {
                logger.debug("Add edge: {}", edge);
                // TODO: cross-wikis graphs
                // TODO: see also the Solr reference serializers
                // TODO: add a default relation Hypergraph:getDefaultRelation
                String fieldName = getFieldName(edge.getRelation());
                if (edge.hasObject()) {
                    // Add index directly to the document so that the documents can be queried by their edges.
                    SolrInputDocument originDocument = getSolrInputDocument(edge.getSubject(), true);
                    addFieldValueOnce(originDocument, fieldName, serializer.serialize(edge.getObject()));

                    // Also index relation IS_CONNECTED_TO because 1) if two vertices are connected by a any other relation,
                    // that can be handy to browse them by the more generic IS_CONNECTED_TO relation, 2) this relation
                    // is used by the user interface GraphMacros to retrieve all vertices that are connected to the current
                    // page.
                    if (!edge.getRelation()
                            .equals(factory.getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME)))
                    {
                        fieldName = getFieldName(Names.IS_CONNECTED_TO_RELATION_NAME);
                        addFieldValueOnce(originDocument, fieldName, serializer.serialize(edge.getObject()));
                    }
                    // TODO: check that originDocument is not added twice, typically when this method
                    //  is called from XWikiEdgeSolrMetadataExtractor
                    save(originDocument);

                    // Add inverse edge, using the IS_CONNECTED_TO relation, so it's not the exact inverse
                    SolrInputDocument destinationDocument = getSolrInputDocument(edge.getObject(), true);
                    addFieldValueOnce(destinationDocument, getFieldName(Names.IS_CONNECTED_TO_RELATION_NAME),
                            serializer.serialize(edge.getSubject()));
                    save(destinationDocument);

                    // Add destinations by transitivity if parameter "transitively" is true
                    /*if (transitively) {
                        XWikiRelation relation = graph.getRelation(edge.getRelation());
                        if (relation.isTransitive()) {
                            List<XWikiEdge> secondLevelEdges =
                                    graph.getEdgesFrom(edge.getObject(), edge.getRelation());
                            for (XWikiEdge secondLevelEdge : secondLevelEdges) {
                                XWikiEdge edgeByTransitivity =
                                        new DefaultXWikiEdge(edge.getSubject(), edge.getRelation(),
                                                secondLevelEdge.getObject(), graph);
                                // Handle "is a" "type" specifically because "A is a B" and "B is a Type" does not
                                // imply "A is a Type" unless we refactor the "is a" relation into "is instance of"
                                // in that case. We need to decide wheter "A is a B" and "A is instance of B" absolutely
                                // need to be covered by two distinct relations or if the two relations share the same
                                // nature.
                                if (!(edge.getRelation()
                                        .equals(graph.getIdentifier(DefaultXWikiGraph.IS_A_RELATION_NAME))
                                        && secondLevelEdge.getObject()
                                        .equals(graph.getIdentifier(DefaultXWikiGraph.TYPE_VERTEX_NAME))))
                                {
                                    index(edgeByTransitivity, false);
                                }
                            }
                        }
                    }*/
                } else if (edge.hasValue()) {
                    // TODO: handle cases where the value is not a string
                    SolrInputDocument originDocument = getSolrInputDocument(edge.getSubject(), true);
                    addFieldValueOnce(originDocument, fieldName, edge.getValue());
                    save(originDocument);
                }
            } catch (GraphException | IOException | SolrServerException e) {
                logger.error("Index edge {}", edge, e);
                throw new GraphException(e);
            }
        }
    }

    protected void save(SolrInputDocument document) throws IOException, SolrServerException
    {
        solrInstanceProvider.get().add(document);
    }

    /**
     * Adapted from {@link org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor} TODO: this should be
     * refactored so that setDocumentFields can be called from XWikiGraph. Create a SolrEntryFactory component? And
     * customized: add FieldUtils.FULLNAME, and FieldUtils.TITLE. Customization: the document fields are set even if the
     * document does not exist yet in the wiki, since this can happen during the indexing of edges: destinations can be
     * indexed due to edges referencing them, prior to get indexed entirely.
     */
    public boolean setDocumentFields(DocumentReference documentReference, SolrInputDocument solrDocument)
            throws Exception
    {
        solrDocument.setField(FieldUtils.WIKI, documentReference.getWikiReference().getName());
        solrDocument.setField(FieldUtils.NAME, documentReference.getName());
        // TODO: check why the FieldUtils.FULLNAME is not present in AbstractSolrMetadataExtractor:setDocumentFields
        solrDocument.setField(FieldUtils.FULLNAME, serializer.serialize(documentReference));

        // Set the fields that are used to query / filter the document hierarchy.
        setHierarchyFields(solrDocument, documentReference.getParent());

        Locale locale = getLocale(documentReference);
        solrDocument.setField(FieldUtils.LOCALE, locale.toString());
        solrDocument.setField(FieldUtils.LANGUAGE, locale.getLanguage());

        XWikiDocument originalDocument = factory.getDocument(documentReference, false);

        if (originalDocument.isNew()) {
            return false;
        } else {
            /**
             * The fields below are already handled set by {@link XWikiVertexSolrMetadataExtractor} for documents.
             */
            //String plainTitle = originalDocument.getRenderedTitle(Syntax.PLAIN_1_0, contextualizer.get());
            //solrDocument.setField(FieldUtils.getFieldName(FieldUtils.TITLE, locale), plainTitle);
            //solrDocument.setField(FieldUtils.HIDDEN, originalDocument.isHidden());
            return true;
        }
    }

    /**
     * Adapted from {@link org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor} TODO: this should be
     * refactored. See setDocumentFields. The SPACES field is reset, otherwise, the spaces add up each time a
     * SolrInputDocument is retrieved via {@link #getSolrInputDocument(EntityReference, boolean)}.
     */
    protected void setHierarchyFields(SolrInputDocument solrDocument, EntityReference path)
    {
        solrDocument.setField(FieldUtils.SPACE_EXACT, serializer.serialize(path));
        List<EntityReference> ancestors = path.getReversedReferenceChain();
        // Skip the wiki reference because we want to index the local space references.
        // Reset the SPACES field so that it does not add up when the SolrInputDocument is retrieved multiple times
        solrDocument.removeField(FieldUtils.SPACES);
        for (int i = 1; i < ancestors.size(); i++) {
            solrDocument.addField(FieldUtils.SPACES, ancestors.get(i).getName());
            String localAncestorReference = serializer.serialize(ancestors.get(i));
            solrDocument.addField(FieldUtils.SPACE_PREFIX, localAncestorReference);
            // We prefix the local ancestor reference with the depth in order to use 'facet.prefix'. We also add a
            // trailing slash in order to distinguish between space names with the same prefix (e.g. 0/Gallery/ and
            // 0/GalleryCode/).
            solrDocument.addField(FieldUtils.SPACE_FACET, (i - 1) + "/" + localAncestorReference + ".");
        }
    }

    /**
     * @see #index(Hyperedge)
     */
    public void unindex(Hyperedge<DocumentReference> edge) throws GraphException
    {
        logger.debug("Remove edge from index: {}", edge);
        // If the edge has no relation, it has no custom index (see #index), so there is no need to remove any
        // index entry.
        if (edge != null && edge.hasRelation() && edge.hasObject()) {
            try {
                SolrInputDocument originDocument = getSolrInputDocument(edge.getSubject(), true);
                unindexValue(originDocument, getFieldName(edge.getRelation()),
                        serializer.serialize(edge.getObject()));

                // Also remove the IS_CONNECTED_TO field in case the destination is not empty
                if (!edge.getRelation().equals(factory.getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME))) {
                    // TODO: the value should be removed only if this was the single edge with this destination
                    String fieldName = getFieldName(Names.IS_CONNECTED_TO_RELATION_NAME);
                    unindexValue(originDocument, fieldName, serializer.serialize(edge.getObject()));

                    // TODO: handle multiple edges with same destination or origin
                    SolrInputDocument destinationDocument = getSolrInputDocument(edge.getObject(), true);
                    unindexValue(destinationDocument, fieldName, serializer.serialize(edge.getSubject()));
                    save(destinationDocument);
                }

                save(originDocument);

                // TODO: should we handle the case of edges pointing at values

            } catch (IOException | SolrServerException e) {
                logger.error("Unindex {}", edge, e);
                throw new GraphException(e);
            }
        }
    }

    public void unindexEdgesPointingAt(DocumentReference destination) throws GraphException
    {
        // All the predecessors of the destination cannot be retrieved via HQL because in the case of
        // transitive relations for instance, the transitive edges are not stored in the SQL database,
        // only in the Solr index.
        // TODO: check what happens with the predecessors which have a real Hyperedge object stored that points
        // to the destination
        // Retrieve all vertices having an edge to destination using the IS_CONNECTED_TO relation which is supposed
        // to be present in all cases.
//        List<DocumentReference> predecessors =
//                getDirectPredecessors(destination, getIdentifier(IS_CONNECTED_TO_RELATION_NAME));
//        for (DocumentReference predecessor : predecessors) {
//            logger.debug("Remove edge from {} to {}", predecessor, destination);
//            try {
//                SolrInputDocument predecessorDocument = getSolrInputDocument(predecessor);
//                unindexValue(predecessorDocument, graph.serializ(destination));
//                index(predecessorDocument);
//            } catch (IOException | SolrServerException e) {
//                logger.error("Unindex edge from {} to {}", predecessor, destination, e);
//                throw new GraphException(e);
//            }
//        }
    }

    public void unindexEdgesUsing(DocumentReference relation) throws GraphException
    {

    }

    protected void unindexValue(SolrInputDocument solrDocument, Object fieldValue)
    {
        for (String fieldName : solrDocument.getFieldNames()) {
            unindexValue(solrDocument, fieldName, fieldValue);
        }
    }

    protected void unindexValue(SolrInputDocument solrDocument, String fieldName, Object fieldValue)
    {
        Collection<Object> fieldValues = solrDocument.getFieldValues(fieldName);
        if (fieldValues != null && fieldValues.contains(fieldValue)) {
            solrDocument.remove(fieldName, fieldValue);
            fieldValues.remove(fieldValue);
            solrDocument.setField(fieldName, fieldValues);
        }
    }
}
