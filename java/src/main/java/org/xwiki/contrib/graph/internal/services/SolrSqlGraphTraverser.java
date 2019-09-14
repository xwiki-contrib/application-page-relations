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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graph.XWikiEdge;
import org.xwiki.contrib.graph.XWikiGraphFactory;
import org.xwiki.contrib.graph.XWikiGraphIndexer;
import org.xwiki.contrib.graph.XWikiGraphTraverser;
import org.xwiki.contrib.graph.XWikiRelation;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiEdge;
import org.xwiki.contrib.graph.internal.model.Names;
import org.xwiki.graph.GraphException;
import org.xwiki.graph.relational.Relation;
import org.xwiki.graph.relational.Set;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.search.solr.internal.api.FieldUtils;
import org.xwiki.search.solr.internal.metadata.LengthSolrInputDocument;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Singleton
@Named("solr-sql")
public class SolrSqlGraphTraverser implements XWikiGraphTraverser
{
    public final static int MAX = 100000;

    public static final String DEFAULT_SORT = FieldUtils.TITLE_SORT + " asc";

    @Inject
    @Named("solr")
    private XWikiGraphIndexer indexer;

    @Inject
    private QueryManager querier;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextualizer;

    @Inject
    private EntityReferenceResolver<SolrDocument> solrResolver;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    @Inject
    private XWikiGraphFactory factory;

    protected Query createQuery(String solrQueryStatement, String sort, int max) throws GraphException
    {
        try {
            Query query = querier.createQuery(solrQueryStatement, "solr");
            if (max == 0) {
                max = MAX;
            }
            query.setLimit(max);
            if (!StringUtils.isEmpty(sort)) {
                query.bindValue("sort", sort);
            } else {
                query.bindValue("sort", "score desc");
            }
            return query;
        } catch (QueryException e) {
            logger.error("createQuery {}", solrQueryStatement, e);
            throw new GraphException(e);
        }
    }

    public List<DocumentReference> getDirectPredecessors(DocumentReference vertex, DocumentReference relation)
            throws GraphException
    {
        // TODO: escape dots in relation name? replaceAll("\\.", "..");
        // TODO: also escape slashes? replace("\\", "\\\\")?
        String statement =
                SolrGraphIndexer.PROPERTY_GRAPH_PREFIX + serializer.serialize(relation) + ":\"" + serializer
                        .serialize(vertex)
                        + "\"";
        return search(statement, SolrSqlGraphTraverser.DEFAULT_SORT, SolrSqlGraphTraverser.MAX);
    }

    public List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference vertex) throws GraphException
    {
        try {
            logger.debug("Get SQL direct predecessors: {}", vertex);
            // NB: an HQL query is used here, not Solr QL, because this method is called by the Solr indexer to
            // compute the index
            // NB: no access right is checked here, because this method is meant to get used internally only.
            List<Object[]> entries = runEdgeHqlQuery(Names.HAS_DESTINATION, serializer.serialize(vertex),
                    vertex.getWikiReference().getName());
            List<DocumentReference> vertices = new ArrayList<>();
            for (Object[] entry : entries) {
                DocumentReference reference = resolver.resolve(entry[0].toString(), vertex);
                vertices.add(reference);
            }
            //logger.debug("Direct predecessors of {} (HQL): ", vertex, vertices);
            return vertices;
        } catch (QueryException e) {
            logger.error("Exception while getting direct predecessors of " + vertex, e);
            throw new GraphException(e);
        }
    }

    public List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference vertex, DocumentReference relation)
            throws GraphException
    {
        try {
            logger.debug("Get SQL direct predecessors: {} {}", vertex, relation);
            // NB: an HQL query is used here, not Solr QL, because this method is called by the Solr indexer to
            // compute the index
            // NB: no access right is checked here, because this method is meant to get used internally only.
            Query query = this.querier.createQuery(
                    "select distinct obj.name, obj.number from BaseObject as obj, StringProperty as hasRelation,"
                            + "StringProperty as hasDestination where obj.className = :className and "
                            + "hasRelation.id.id = obj.id and hasRelation.id.name = :hasRelation and "
                            + "hasDestination.id.id = obj.id and hasDestination.id.name = :hasDestination and "
                            + "hasRelation.value  = :relation and hasDestination.value = :destination", Query.HQL);
            query = query.bindValue("className", DefaultXWikiEdge.EDGE_VERTEX_ID)
                    .bindValue("hasRelation", Names.HAS_RELATION)
                    .bindValue("hasDestination", Names.HAS_DESTINATION)
                    .bindValue("relation", serializer.serialize(relation))
                    .bindValue("destination", serializer.serialize(vertex));
            query.setWiki(vertex.getWikiReference().getName());
            List<Object[]> entries = query.execute();
            List<DocumentReference> vertices = new ArrayList<>();
            for (Object[] entry : entries) {
                DocumentReference reference = resolver.resolve(entry[0].toString(), vertex);
                vertices.add(reference);
            }
            return vertices;
        } catch (QueryException e) {
            logger.error("Exception while getting direct predecessors of " + vertex, e);
            throw new GraphException(e);
        }
    }

    public XWikiEdge getEdge(DocumentReference vertex, DocumentReference relation, DocumentReference destination)
            throws GraphException
    {
        XWikiDocument page = factory.getDocument(vertex, false);
        for (Triple<EntityReference, Class, Class> edgeType : factory.getEdgeClasses()) {
            for (BaseObject baseObject : page.getXObjects(edgeType.getLeft())) {
                // TODO: check if and why getXObjects can return null elements
                if (baseObject != null) {
                    XWikiEdge edge = factory.createEdge(baseObject);
                    if (destination.equals(edge.getDestination()) && relation.equals(edge.getRelation())) {
                        return edge;
                    }
                }
            }
        }
        return null;
    }

    // TODO: in theory, this should return all edges from vertex to destination and inversely, to be
    //  in symetry with removeEdges(origin, destination)
    public List<XWikiEdge> getEdges(DocumentReference origin, DocumentReference destination)
            throws GraphException
    {
        // TODO: use functional interface to share common code with getEdge()
        List<XWikiEdge> edges = new ArrayList<>();
        XWikiDocument page = factory.getDocument(origin, false);
        for (Triple<EntityReference, Class, Class> edgeType : factory.getEdgeClasses()) {
            for (BaseObject baseObject : page.getXObjects(edgeType.getLeft())) {
                if (baseObject != null) {
                    XWikiEdge edge = factory.createEdge(baseObject);
                    if (destination.equals(edge.getDestination())) {
                        edges.add(edge);
                    }
                }
            }
        }
        return edges;
    }

    public List<XWikiEdge> getEdgesFrom(DocumentReference vertex) throws GraphException
    {
        return getEdgesFrom(factory.getDocument(vertex, false));
    }

    public List<XWikiEdge> getEdgesFrom(DocumentReference vertex, DocumentReference relation) throws GraphException
    {
        return getEdgesFrom(factory.getDocument(vertex, false), relation);
    }

    public List<XWikiEdge> getEdgesFrom(XWikiDocument page) throws GraphException
    {
        // TODO: share code with #getEdgesFrom(XWikiDocument, DocumentReference)
        List<XWikiEdge> edges = new ArrayList<>();
        for (Triple<EntityReference, Class, Class> edgeType : factory.getEdgeClasses()) {
            for (BaseObject baseObject : page.getXObjects(edgeType.getLeft())) {
                if (baseObject != null) {
                    edges.add(factory.createEdge(baseObject));
                }
            }
        }
        return edges;
    }

    // TODO: use optional single parameter instead of a vargs?
    public List<XWikiEdge> getEdgesFrom(XWikiDocument page, DocumentReference relation) throws GraphException
    {
        List<XWikiEdge> edges = new ArrayList<>();
        if (relation == null) {
            return edges;
        }
        for (Triple<EntityReference, Class, Class> edgeType : factory.getEdgeClasses()) {
            for (BaseObject baseObject : page.getXObjects(edgeType.getLeft())) {
                if (baseObject != null) {
                    // Add edge only if its relation matches with the one given as parameter.
                    if (serializer.serialize(relation).equals(baseObject.getStringValue(Names.HAS_RELATION))) {
                        edges.add(factory.createEdge(baseObject));
                    }
                }
            }
        }
        return edges;
    }

    public XWikiEdge getFirstEdgeFrom(DocumentReference vertex, DocumentReference relation) throws GraphException
    {
        for (XWikiEdge edge : getEdgesFrom(vertex, relation)) {
            return edge;
        }
        return null;
    }

    public List<DocumentReference> getNeighbours(DocumentReference vertex) throws GraphException
    {
        // TODO: escape dots?
        // TODO: we may need to escape anti-slash: #set ($subjectId = $subjectId.replaceAll('\\', '\\\\'))
        return run(getNeighboursQuery(vertex));
    }

    public Query getNeighboursQuery(DocumentReference vertex) throws GraphException
    {
        String statement =
                SolrGraphIndexer.PROPERTY_GRAPH_PREFIX +
                        serializer.serialize(factory.getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME)) + ":\"" +
                        serializer.serialize(vertex) + "\"";
        return createQuery(statement, DEFAULT_SORT, MAX);
    }

    public List<XWikiRelation> getRelations(DocumentReference vertex,
            List<? extends Relation<DocumentReference>> relations) throws GraphException
    {
        List<XWikiRelation> compatibleRelations = new ArrayList<>();
        LengthSolrInputDocument vertexSolr = getSolrInputDocument(vertex);
        for (Relation<DocumentReference> relation : relations) {
            String domain = relation.getDomain();
            if (!StringUtils.isEmpty(domain)) {
                if (domain.equals(Set.ANY.getLabel())) {
                    compatibleRelations.add((XWikiRelation) relation);
                } else {
                    int idx = domain.indexOf(":");
                    if (idx > 0) {
                        String fieldName = domain.substring(0, idx);
                        String fieldValue = domain.substring(idx + 1).replaceAll("\"", "");
                        //TODO: do not append "string" manually
                        Collection<Object> values = vertexSolr.getFieldValues(fieldName + "_string");
                        if (values != null && values.contains(fieldValue)) {
                            compatibleRelations.add((XWikiRelation) relation);
                        }
                    }
                }
            } else {
                // if the applies-to constraint is empty, consider that the relation applies to any vertex
                compatibleRelations.add((XWikiRelation) relation);
            }
        }
        return compatibleRelations;
    }

    public LengthSolrInputDocument getSolrInputDocument(DocumentReference identifier) throws GraphException
    {
        return ((SolrGraphIndexer) indexer).getSolrInputDocument(identifier, true);
    }

    public List<DocumentReference> run(Query query) throws GraphException
    {
        SolrDocumentList results = null;
        try {
            List<Object> searchResponse = query.execute();
            if (searchResponse != null && searchResponse.size() > 0) {
                QueryResponse response = (QueryResponse) searchResponse.get(0);
                results = response.getResults();
            }

            List<DocumentReference> vertices = new ArrayList<>();
            if (results == null) {
                return vertices;
            }
            for (SolrDocument result : results) {
                EntityType type = EntityType.valueOf((String) result.get(FieldUtils.TYPE));
                EntityReference reference = solrResolver.resolve(result, type);
                // TODO: only documents are expected, we may enforce this
                // TODO: uniqueness should be handled at the query level directly
                if (!vertices.contains(reference)) {
                    // We consider that all DocumentReferences can be retrieved, it's only the access to the
                    // referenced page itself that will get denied, otherwise this can raise issues with
                    // Solr pagination.
                    // authorizer.checkAccess(Right.VIEW, context.getUserReference(), reference);
                    // TODO: check it's ok to create a DocumentReference like this in all cases
                    vertices.add(new DocumentReference(reference));
                }
            }
            return vertices;
        } catch (QueryException e) {
            logger.error("Exception while running query: {}", query, e);
            throw new GraphException(e);
        }
    }

    public List<Object[]> runEdgeHqlQuery(String property, String destination, String wiki)
            throws QueryException
    {
        Query query = this.querier.createQuery(
                "select distinct obj.name, obj.number from BaseObject as obj, StringProperty as prop where "
                        + "obj.className = :className and prop.id.id = obj.id and prop.id.name = :property and "
                        + "prop.value = :destination", Query.HQL);
        query = query.bindValue("className", DefaultXWikiEdge.EDGE_VERTEX_ID).bindValue("property", property)
                .bindValue("destination", destination);
        query.setWiki(wiki);
        return query.execute();
    }

    public List<DocumentReference> search(String text, Relation<DocumentReference> relation) throws GraphException
    {
        String image = relation.getImage();
        String sql = "\"" + text + "\"";
        if (!StringUtils.isEmpty(image) && !image.equals(Set.ANY.getLabel())) {
            sql += " AND " + image;
        }
        String sort = "score desc";
        // If input is empty, return results sorted alphabetically
        if (StringUtils.isEmpty(text)) {
            sort = "title_sort asc";
        }
        return search(sql, sort, 30);
    }

    public List<DocumentReference> search(String text) throws GraphException
    {
        String sql = "\"" + text + "\"";
        return search(sql, "score desc", 30);
    }

    public List<DocumentReference> search(String sql, String sort, int max) throws GraphException
    {
        XWikiContext context = contextualizer.get();
        String wikiId = context.getWikiId();
        sql = "wiki:\"" + wikiId + "\"  AND type:DOCUMENT AND " + sql;
        Query query = createQuery(sql, sort, max);
        return run(query);
    }
}
