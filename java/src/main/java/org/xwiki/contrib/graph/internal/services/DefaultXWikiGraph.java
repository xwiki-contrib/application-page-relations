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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graph.XWikiEdge;
import org.xwiki.contrib.graph.XWikiGraph;
import org.xwiki.contrib.graph.XWikiGraphFactory;
import org.xwiki.contrib.graph.XWikiGraphTraverser;
import org.xwiki.contrib.graph.XWikiRelation;
import org.xwiki.contrib.graph.XWikiVertex;
import org.xwiki.contrib.graph.internal.model.BooleanXWikiEdge;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiEdge;
import org.xwiki.contrib.graph.internal.model.Names;
import org.xwiki.hypergraph.GraphException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.query.QueryException;
import org.xwiki.security.authorization.AccessDeniedException;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseObjectReference;

@Component
@Singleton
@Named("default")
@Unstable
public class DefaultXWikiGraph implements XWikiGraph
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextualizer;

    @Inject
    private AuthorizationManager authorizer;

    @Inject
    @Named("solr-sql")
    private XWikiGraphTraverser traverser;

    @Inject
    private XWikiGraphFactory factory;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    /**
     * A FunctionInterface is used to handle both cases where the destination is either a DocumentReference or a scalar
     * as a String, Boolean, Date, ...
     */
    public void addEdge(DocumentReference origin, DocumentReference relation, Object destinationOrValue,
            EdgeTargeter targeter) throws GraphException
    {
        //  TODO: should we allow the creation of an edge that involves inexistent origin, relation or destination or throw an exception?
        if (origin == null || relation == null || destinationOrValue == null) {
            throw new IllegalArgumentException(
                    "addEdge: " + origin + " - " + relation + " - " + destinationOrValue);
        }

        try {
            XWikiContext context = contextualizer.get();
            // The authorizer will throw an exception in case edit is not allowed
            authorizer.checkAccess(Right.EDIT, context.getUserReference(), origin);
            XWikiDocument clone = factory.getDocument(origin, true);
            // NB: warning: don't remove the edge operations below, because they set some properties
            // in the underlying BaseObject, which are meant to be saved when the page is saved
            XWikiEdge edge = factory.createEdge(clone, destinationOrValue);
            edge.setRelation(relation);
            targeter.setTarget(edge, destinationOrValue);
            logger.debug("Add edge: {}", edge);
            factory.saveDocument(clone, "graph.edge.history.index" + destinationOrValue);
        } catch (AccessDeniedException e) {
            logger.error("addEdge", e);
            throw new GraphException(e);
        }
    }

    public void addEdge(DocumentReference subject, DocumentReference relation, Object object)
            throws GraphException
    {
        if (object instanceof DocumentReference) {
            DocumentReference destination = (DocumentReference) object;
            addEdge(subject, relation, destination, (XWikiEdge edge, Object target) -> edge
                    .setObject((DocumentReference) target));
        } else {
            addEdge(subject, relation, object, (XWikiEdge edge, Object target) -> edge
                    .setValue(target));
        }
    }

    public void addEdge(DocumentReference origin, DocumentReference destination) throws GraphException
    {
        addEdge(origin, factory.getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME), destination);
    }

    public void addEdgeOnce(DocumentReference subject, DocumentReference relation, Object object)
            throws GraphException
    {
        // TODO: add synchronization mechanism to make sure that there is no equivalent edge
        //  being created while the check if getting performed.
        List<XWikiEdge> edges = traverser.getEdgesFrom(subject, relation);
        for (XWikiEdge edge : edges) {
            if (object instanceof DocumentReference) {
                if (edge.hasObject() && edge.getObject().equals(object)) {
                    return;
                }
            } else {
                if (edge.hasValue() && edge.getValue().equals(object)) {
                    return;
                }
            }
        }
        addEdge(subject, relation, object);
    }

    public void addRelation(DocumentReference identifier, String name, String domain, String image)
            throws GraphException
    {
        try {
            XWikiContext context = contextualizer.get();
            XWiki xwiki = context.getWiki();
            if (!xwiki.exists(identifier, context)) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), identifier);
                addVertex(identifier, name, factory.getIdentifier(Names.RELATION_VERTEX_NAME));
                addEdge(identifier, factory.getIdentifier(Names.HAS_DOMAIN_RELATION_NAME), domain);
                addEdge(identifier, factory.getIdentifier(Names.HAS_IMAGE_RELATION_NAME), image);
            } else {
                throw new GraphException("A vertex with reference " + identifier + " already exists in the graph.");
            }
        } catch (AccessDeniedException e) {
            logger.error("Exception while adding relation", e);
            throw new GraphException(e);
        }
    }

    public void addVertex(DocumentReference identifier) throws GraphException
    {
        addVertex(identifier, "");
    }

    public void addVertex(DocumentReference identifier, String name) throws GraphException
    {
        addVertex(identifier, name, null);
    }

    public void addVertex(DocumentReference identifier, String name, DocumentReference type)
            throws GraphException
    {
        logger.debug("Add vertex: {}", identifier);
        try {
            XWikiContext context = contextualizer.get();
            XWiki xwiki = context.getWiki();
            if (!xwiki.exists(identifier, context)) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), identifier);
                XWikiDocument page = factory.getDocument(identifier, false);
                if (!StringUtils.isEmpty(name)) {
                    page.setTitle(name);
                }
                // Save document as is, in case no type is passed
                xwiki.saveDocument(page, "graph.index-vertex", true, context);
                if (type != null) {
                    // In case a type is passed, add an Hyperedge pointing at that type (the document will get saved again)
                    addEdge(identifier, factory.getIdentifier(Names.IS_A_RELATION_NAME), type);
                }
            } else {
                throw new GraphException("A vertex with reference " + identifier + " already exists in the graph.");
            }
        } catch (XWikiException | AccessDeniedException e) {
            logger.error("Exception while adding vertex", e);
            throw new GraphException(e);
        }
    }

    /**
     * Not implemented: this corresponds to edges stored in their own document.
     */
    public XWikiEdge getEdge(DocumentReference identifier)
    {
        throw new NotImplementedException();
    }

    protected XWikiEdge getEdge(XWikiDocument origin, int objectIndex) throws GraphException
    {
        BaseObjectReference objectReference =
                origin.getXObject(DefaultXWikiEdge.EDGE_XCLASS_REFERENCE, objectIndex).getReference();
        return getEdge(origin, objectReference);
    }

    public XWikiEdge getEdge(XWikiDocument origin, ObjectReference reference) throws GraphException
    {
        BaseObject object = origin.getXObject(reference);
        return factory.createEdge(object);
    }

    public XWikiEdge getEdge(DocumentReference subject, DocumentReference relation, DocumentReference object)
            throws GraphException
    {
        // Not optimized implementation but it avoids duplicating code for now
        List<XWikiEdge> edges = traverser.getEdges(subject, object);
        for (XWikiEdge edge : edges) {
            if (relation.equals(edge.getRelation())) {
                return edge;
            }
        }
        return null;
    }

    public XWikiRelation getRelation(DocumentReference identifier) throws GraphException
    {
        // TODO: a cache might be needed, however the XWikiDocuments corresponding to the relations are likely
        //  to be cached already, but we'd better make it sure
        XWikiEdge domain =
                traverser.getFirstEdgeFrom(identifier, factory.getIdentifier(Names.HAS_DOMAIN_RELATION_NAME)),
                image = traverser.getFirstEdgeFrom(identifier, factory.getIdentifier(Names.HAS_IMAGE_RELATION_NAME)),
                isTransitiveEdge =
                        traverser.getFirstEdgeFrom(identifier, factory.getIdentifier(Names.IS_TRANSTIVE_RELATION_NAME));
        String domainAsString = null, imageAsString = null;
        boolean transitive = false;

        if (domain != null && domain.getValue() != null) {
            domainAsString = domain.getValue().toString();
        }
        if (image != null && image.getValue() != null) {
            imageAsString = image.getValue().toString();
        }
        if (isTransitiveEdge != null && ((BooleanXWikiEdge) isTransitiveEdge).getValue()) {
            transitive = true;
        }
        return factory.createRelation(identifier, domainAsString, imageAsString, transitive);
    }

    public List<XWikiRelation> getRelations() throws GraphException
    {
        List<DocumentReference> relationReferences =
                traverser.getDirectPredecessors(factory.getIdentifier(Names.RELATION_VERTEX_NAME),
                        factory.getIdentifier(Names.IS_A_RELATION_NAME));
        List<XWikiRelation> relations = new ArrayList<>();
        for (DocumentReference identifier : relationReferences) {
            relations.add(getRelation(identifier));
        }
        return relations;
    }

    public XWikiVertex getVertex(DocumentReference identifier)
    {
        // TODO: throw a GraphException when a "system" Vertex is requested but not found, for example:
        //  the relation "HasImage", the vertex "Type", etc.
        return factory.createVertex(identifier);
    }

    public void removeEdge(DocumentReference vertex1, DocumentReference vertex2) throws GraphException
    {
        removeEdges(vertex1, vertex2);
    }

    public void removeEdge(DocumentReference subject, DocumentReference relation, Object object)
            throws GraphException
    {
        XWikiContext context = contextualizer.get();
        // TODO: below we consider that edges are necessarily stored directly on the origin, but later on
        //  edges could also have their own page with own access rights.

        if (object instanceof DocumentReference) {
            XWikiEdge edge = getEdge(subject, relation, (DocumentReference) object);
            if (edge != null) {
                try {
                    authorizer.checkAccess(Right.EDIT, context.getUserReference(), subject);
                    XWikiDocument clone = factory.getDocument(subject, true);
                    clone.removeXObject(edge.getBaseObject());
                    context.getWiki().saveDocument(clone, "graph.edge.history.remove", true, context);
                } catch (XWikiException | AccessDeniedException e) {
                    logger.error("Exception while removing edge", e);
                    throw new GraphException(e);
                }
            }
        } else {
            throw new NotImplementedException();
        }
    }

    public void removeEdges(DocumentReference vertex) throws GraphException
    {
        removeEdgesTo(vertex);
    }

    public void removeEdges(DocumentReference subject, DocumentReference object) throws GraphException
    {
        XWikiContext context = contextualizer.get();
        List<XWikiEdge> edges = traverser.getEdges(subject, object);
        try {
            if (edges.size() > 0) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), subject);
                XWikiDocument clone = factory.getDocument(subject, true);
                for (XWikiEdge edge : edges) {
                    clone.removeXObject(edge.getBaseObject());
                }
                context.getWiki().saveDocument(clone, "graph.edge.history.remove", true, context);
            }

            // Remove edges from destination to origin, if any
            // TODO: factorize code with above
            // TODO: check it's ok to retrieve BaseObjects from an XWikiDocument, then to remove them from a clone
            //  of the XWikiDocument
            edges = traverser.getEdges(object, subject);
            if (edges.size() > 0) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), object);
                XWikiDocument clone = factory.getDocument(object, true);
                for (XWikiEdge edge : edges) {
                    clone.removeXObject(edge.getBaseObject());
                }
                context.getWiki().saveDocument(clone, "graph.edge.history.remove", true, context);
            }
        } catch (XWikiException | AccessDeniedException e) {
            logger.error("Exception while removing edge", e);
            throw new GraphException(e);
        }

        //if (edge == null) {
        // If Hyperedge was not found from origin, look for the ones which start from destination
        //clone = getDocument(destination, true);
        //edge = clone.getXObject(EDGE_VERTEX_REFERENCE, HAS_DESTINATION, serialize(destination), false);
        //}

    }

    public void removeEdgesFrom(DocumentReference subject)
    {
        throw new NotImplementedException();
    }

    /**
     * Remove all stored edges having the given reference as destination. TODO: also remove the edges not stored but
     * indexed via transitivity
     */
    public void removeEdgesTo(DocumentReference object) throws GraphException
    {
        XWikiContext context = contextualizer.get();
        XWiki wiki = context.getWiki();
        // TODO: we may use the Solr index instead?
        List<DocumentReference> predecessors = traverser.getDirectPredecessorsViaHql(object);
        for (DocumentReference predecessor : predecessors) {
            XWikiDocument predecessorDocument = factory.getDocument(predecessor, true);
            List<XWikiEdge> edges = traverser.getEdgesFrom(predecessorDocument);
            List<BaseObject> toBeRemoved = new ArrayList<>();
            for (XWikiEdge edge : edges) {
                if (object.equals(edge.getObject())) {
                    toBeRemoved.add(edge.getBaseObject());
                }
            }
            for (BaseObject baseObject : toBeRemoved) {
                predecessorDocument.removeXObject(baseObject);
            }
            try {
                wiki.saveDocument(predecessorDocument, "graph.history.remove-edge " + object, true, context);
            } catch (XWikiException e) {
                logger.error("removeEdgesTo {} from {}", object, predecessor, e);
                throw new GraphException(e);
            }
        }
    }

    public void removeEdgesWith(DocumentReference relation) throws GraphException
    {
        // TODO: use common code with removeEdgesTo, the code only differs by the method to be
        // called on edge: it's edge.getRelation() and edge.getObject()
        // TODO: another implementation could consist in using the default relation "IS_CONNECTED_TO" rather
        // than removing the edges completely

        XWikiContext context = contextualizer.get();
        XWiki wiki = context.getWiki();
        List<DocumentReference> predecessors = traverser.getDirectPredecessorsViaHql(relation);
        for (DocumentReference predecessor : predecessors) {
            XWikiDocument predecessorDocument = factory.getDocument(predecessor, true);
            List<XWikiEdge> edges = traverser.getEdgesFrom(predecessorDocument);
            List<BaseObject> toBeRemoved = new ArrayList<>();
            for (XWikiEdge edge : edges) {
                if (relation.equals(edge.getRelation())) {
                    toBeRemoved.add(edge.getBaseObject());
                }
            }
            for (BaseObject baseObject : toBeRemoved) {
                predecessorDocument.removeXObject(baseObject);
            }
            try {
                wiki.saveDocument(predecessorDocument, "graph.history.remove-edge " + relation, true, context);
            } catch (XWikiException e) {
                logger.error("removeEdgesTo {} from {}", relation, predecessor, e);
                throw new GraphException(e);
            }
        }
    }

    public void removeVertex(DocumentReference identifier) throws GraphException
    {
        XWikiContext context = contextualizer.get();
        XWiki xwiki = context.getWiki();
        if (!xwiki.exists(identifier, context)) {
            logger.warn("Attempt to remove an inexistent vertex: {}", identifier);
            return;
        }
        try {
            XWikiDocument page = factory.getDocument(identifier, true);
            xwiki.deleteDocument(page, context);
        } catch (XWikiException e) {
            logger.error("Exception while removing vertex " + identifier, e);
            throw new GraphException(e);
        }
    }

    public void updateEdge(DocumentReference originVertex, int objectIndex, DocumentReference originalReference,
            DocumentReference newReference, String edgeProperty) throws GraphException
    {
        try {
            XWikiContext context = contextualizer.get();
            authorizer.checkAccess(Right.EDIT, context.getUserReference(), originVertex);
            // Clone the document first because its objects will get updated
            XWikiDocument page = context.getWiki().getDocument(originVertex, context).clone();
            logger.debug("Update edge property {} of {} from {} to {}", edgeProperty, originVertex,
                    originalReference, newReference);
            XWikiEdge edge = getEdge(page, objectIndex);
            if (edgeProperty.equals(Names.HAS_DESTINATION)) {
                edge.setObject(newReference);
            } else if (edgeProperty.equals(Names.HAS_RELATION)) {
                edge.setRelation(newReference);
            } else {
                throw new IllegalArgumentException("Illegal edge property name: " + edgeProperty);
            }
            context.getWiki().saveDocument(page, "graph.edge.history.index" + newReference, true,
                    context);
        } catch (AccessDeniedException | XWikiException e) {
            logger.error("updateEdge {} {} {}", originVertex, originalReference, newReference, e);
            throw new GraphException(e);
        }
    }

    public void updateEdges(DocumentReference originalVertex, DocumentReference newVertex) throws GraphException
    {
        updateEdgesTo(originalVertex, newVertex);
    }

    protected void updateEdges(DocumentReference originalReference, DocumentReference newReference, String edgeProperty)
            throws GraphException
    {
        // We could get the BaseObjects directly, so that we don't iterate twice to get the target Edges,
        // just like in #updateEdgesWith
        try {
            String wikiId = originalReference.getWikiReference().getName();
            List<Object[]> entries =
                    traverser.runEdgeHqlQuery(edgeProperty, serializer.serialize(originalReference), wikiId);
            for (Object[] entry : entries) {
                DocumentReference originVertex = resolver.resolve(entry[0].toString(), originalReference);
                int objectIndex = (int) entry[1];
                // TODO: below we consider that edges are stored directly in the vertex document, but actually they could be
                //  stored in their own page with own access rights
                updateEdge(originVertex, objectIndex, originalReference, newReference, edgeProperty);
            }
        } catch (QueryException e) {
            logger.error("updateEdgesTo", e);
            throw new GraphException(e);
        }
    }

    public void updateEdgesTo(DocumentReference originalObject, DocumentReference otherObject)
            throws GraphException
    {
        updateEdges(originalObject, otherObject, Names.HAS_DESTINATION);
    }

    public void updateEdgesWith(DocumentReference originalRelation, DocumentReference otherRelation)
            throws GraphException
    {
        updateEdges(originalRelation, otherRelation, Names.HAS_RELATION);
    }

    @FunctionalInterface
    interface EdgeTargeter
    {
        void setTarget(XWikiEdge edge, Object target);
    }
}
