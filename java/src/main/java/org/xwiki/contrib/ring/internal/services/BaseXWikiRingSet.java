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
package org.xwiki.contrib.ring.internal.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiRingSet;
import org.xwiki.contrib.ring.XWikiRingTraverser;
import org.xwiki.contrib.ring.XWikiTermFactory;
import org.xwiki.contrib.ring.XWikiRelation;
import org.xwiki.contrib.ring.XWikiTerm;
import org.xwiki.contrib.ring.internal.model.BaseXWikiRing;
import org.xwiki.contrib.ring.internal.model.BooleanXWikiRing;
import org.xwiki.contrib.ring.internal.model.Names;
import io.ring.RingException;

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
public class BaseXWikiRingSet implements XWikiRingSet
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextualizer;

    @Inject
    private AuthorizationManager authorizer;

    @Inject
    @Named("solr-sql")
    private XWikiRingTraverser traverser;

    @Inject
    private XWikiTermFactory factory;

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
    public void addRing(DocumentReference origin, DocumentReference relation, Object destinationOrValue,
            EdgeTargeter targeter) throws RingException
    {
        //  TODO: should we allow the creation of an edge that involves inexistent origin, relation or destination or throw an exception?
        if (origin == null || relation == null || destinationOrValue == null) {
            throw new IllegalArgumentException(
                    "addRing: " + origin + " - " + relation + " - " + destinationOrValue);
        }

        try {
            XWikiContext context = contextualizer.get();
            // The authorizer will throw an exception in case edit is not allowed
            authorizer.checkAccess(Right.EDIT, context.getUserReference(), origin);
            XWikiDocument clone = factory.getDocument(origin, true);
            // NB: warning: don't remove the edge operations below, because they set some properties
            // in the underlying BaseObject, which are meant to be saved when the page is saved
            XWikiRing edge = factory.createEdge(clone, destinationOrValue);
            edge.setRelation(relation);
            targeter.setTarget(edge, destinationOrValue);
            logger.debug("Add edge: {}", edge);
            factory.saveDocument(clone, "ring.edge.history.index" + destinationOrValue);
        } catch (AccessDeniedException e) {
            logger.error("addRing", e);
            throw new RingException(e);
        }
    }

    public void addRing(DocumentReference referent, DocumentReference relation, Object relatum)
            throws RingException
    {
        if (relatum instanceof DocumentReference) {
            DocumentReference destination = (DocumentReference) relatum;
            addRing(referent, relation, destination, (XWikiRing edge, Object target) -> edge
                    .setRelatum((DocumentReference) target));
        } else {
            addRing(referent, relation, relatum, (XWikiRing edge, Object target) -> edge
                    .setValue(target));
        }
    }

    public void addRing(DocumentReference referent, DocumentReference relatum) throws RingException
    {
        addRing(referent, factory.getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME), relatum);
    }

    public void addRingOnce(DocumentReference referent, DocumentReference relation, Object relatum)
            throws RingException
    {
        // TODO: add synchronization mechanism to make sure that there is no equivalent edge
        //  being created while the check if getting performed.
        List<XWikiRing> edges = traverser.getRingsFrom(referent, relation);
        for (XWikiRing edge : edges) {
            if (relatum instanceof DocumentReference) {
                if (edge.hasRelatum() && edge.getRelatum().equals(relatum)) {
                    return;
                }
            } else {
                if (edge.hasValue() && edge.getValue().equals(relatum)) {
                    return;
                }
            }
        }
        addRing(referent, relation, relatum);
    }

    public void addRelation(DocumentReference identifier, String name, String domain, String image)
            throws RingException
    {
        try {
            XWikiContext context = contextualizer.get();
            XWiki xwiki = context.getWiki();
            if (!xwiki.exists(identifier, context)) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), identifier);
                addVertex(identifier, name, factory.getIdentifier(Names.RELATION_TERM_NAME));
                addRing(identifier, factory.getIdentifier(Names.HAS_DOMAIN_RELATION_NAME), domain);
                addRing(identifier, factory.getIdentifier(Names.HAS_IMAGE_RELATION_NAME), image);
            } else {
                throw new RingException("A vertex with reference " + identifier + " already exists in the ring.");
            }
        } catch (AccessDeniedException e) {
            logger.error("Exception while adding relation", e);
            throw new RingException(e);
        }
    }

    public void addTerm(DocumentReference identifier) throws RingException
    {
        addTerm(identifier, "");
    }

    public void addTerm(DocumentReference identifier, String name) throws RingException
    {
        addVertex(identifier, name, null);
    }

    public void addVertex(DocumentReference identifier, String name, DocumentReference type)
            throws RingException
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
                xwiki.saveDocument(page, "ring.index-vertex", true, context);
                if (type != null) {
                    // In case a type is passed, add an Ring pointing at that type (the document will get saved again)
                    addRing(identifier, factory.getIdentifier(Names.IS_A_RELATION_NAME), type);
                }
            } else {
                throw new RingException("A vertex with reference " + identifier + " already exists in the ring.");
            }
        } catch (XWikiException | AccessDeniedException e) {
            logger.error("Exception while adding vertex", e);
            throw new RingException(e);
        }
    }

    /**
     * Not implemented: this corresponds to edges stored in their own document.
     */
    public XWikiRing getRing(DocumentReference identifier)
    {
        throw new NotImplementedException();
    }

    protected XWikiRing getRing(XWikiDocument origin, int objectIndex) throws RingException
    {
        BaseObjectReference objectReference =
                origin.getXObject(BaseXWikiRing.EDGE_XCLASS_REFERENCE, objectIndex).getReference();
        return getRing(origin, objectReference);
    }

    public XWikiRing getRing(XWikiDocument origin, ObjectReference reference) throws RingException
    {
        BaseObject object = origin.getXObject(reference);
        return factory.createEdge(object);
    }

    public XWikiRing getRing(DocumentReference subject, DocumentReference relation, DocumentReference object)
            throws RingException
    {
        // Not optimized implementation but it avoids duplicating code for now
        List<XWikiRing> edges = traverser.getRings(subject, object);
        for (XWikiRing edge : edges) {
            if (relation.equals(edge.getRelation())) {
                return edge;
            }
        }
        return null;
    }

    public XWikiRelation getRelation(DocumentReference identifier) throws RingException
    {
        // TODO: a cache might be needed, however the XWikiDocuments corresponding to the relations are likely
        //  to be cached already, but we'd better make it sure
        XWikiRing domain =
                traverser.getFirstRingFrom(identifier, factory.getIdentifier(Names.HAS_DOMAIN_RELATION_NAME)),
                image = traverser.getFirstRingFrom(identifier, factory.getIdentifier(Names.HAS_IMAGE_RELATION_NAME)),
                isTransitiveEdge =
                        traverser.getFirstRingFrom(identifier, factory.getIdentifier(Names.IS_TRANSTIVE_RELATION_NAME));
        String domainAsString = null, imageAsString = null;
        boolean transitive = false;

        if (domain != null && domain.getValue() != null) {
            domainAsString = domain.getValue().toString();
        }
        if (image != null && image.getValue() != null) {
            imageAsString = image.getValue().toString();
        }
        if (isTransitiveEdge != null && ((BooleanXWikiRing) isTransitiveEdge).getValue()) {
            transitive = true;
        }
        return factory.createRelation(identifier, domainAsString, imageAsString, transitive);
    }

    public List<XWikiRelation> getRelations() throws RingException
    {
        List<DocumentReference> relationReferences =
                traverser.getDirectPredecessors(factory.getIdentifier(Names.RELATION_TERM_NAME),
                        factory.getIdentifier(Names.IS_A_RELATION_NAME));
        List<XWikiRelation> relations = new ArrayList<>();
        for (DocumentReference identifier : relationReferences) {
            relations.add(getRelation(identifier));
        }
        return relations;
    }

    public XWikiTerm getTerm(DocumentReference identifier)
    {
        // TODO: throw a RingException when a "system" Term is requested but not found, for example:
        //  the relation "HasImage", the vertex "Type", etc.
        return factory.createVertex(identifier);
    }

    public void removeRing(DocumentReference referent, DocumentReference relatum) throws RingException
    {
        removeRings(referent, relatum);
    }

    public void removeRing(DocumentReference referent, DocumentReference relation, Object relatum)
            throws RingException
    {
        XWikiContext context = contextualizer.get();
        // TODO: below we consider that edges are necessarily stored directly on the origin, but later on
        //  edges could also have their own page with own access rights.

        if (relatum instanceof DocumentReference) {
            XWikiRing edge = getRing(referent, relation, (DocumentReference) relatum);
            if (edge != null) {
                try {
                    authorizer.checkAccess(Right.EDIT, context.getUserReference(), referent);
                    XWikiDocument clone = factory.getDocument(referent, true);
                    clone.removeXObject(edge.getBaseObject());
                    context.getWiki().saveDocument(clone, "ring.edge.history.remove", true, context);
                } catch (XWikiException | AccessDeniedException e) {
                    logger.error("Exception while removing edge", e);
                    throw new RingException(e);
                }
            }
        } else {
            throw new NotImplementedException();
        }
    }

    public void removeRings(DocumentReference vertex) throws RingException
    {
        removeRingsTo(vertex);
    }

    public void removeRings(DocumentReference termOne, DocumentReference termTwo) throws RingException
    {
        XWikiContext context = contextualizer.get();
        List<XWikiRing> edges = traverser.getRings(termOne, termTwo);
        try {
            if (edges.size() > 0) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), termOne);
                XWikiDocument clone = factory.getDocument(termOne, true);
                for (XWikiRing edge : edges) {
                    clone.removeXObject(edge.getBaseObject());
                }
                context.getWiki().saveDocument(clone, "ring.edge.history.remove", true, context);
            }

            // Remove edges from destination to origin, if any
            // TODO: factorize code with above
            // TODO: check it's ok to retrieve BaseObjects from an XWikiDocument, then to remove them from a clone
            //  of the XWikiDocument
            edges = traverser.getRings(termTwo, termOne);
            if (edges.size() > 0) {
                authorizer.checkAccess(Right.EDIT, context.getUserReference(), termTwo);
                XWikiDocument clone = factory.getDocument(termTwo, true);
                for (XWikiRing edge : edges) {
                    clone.removeXObject(edge.getBaseObject());
                }
                context.getWiki().saveDocument(clone, "ring.edge.history.remove", true, context);
            }
        } catch (XWikiException | AccessDeniedException e) {
            logger.error("Exception while removing edge", e);
            throw new RingException(e);
        }

        //if (edge == null) {
        // If Ring was not found from origin, look for the ones which start from destination
        //clone = getDocument(destination, true);
        //edge = clone.getXObject(EDGE_VERTEX_REFERENCE, HAS_DESTINATION, serialize(destination), false);
        //}

    }

    public void removeRingsFrom(DocumentReference referent)
    {
        throw new NotImplementedException();
    }

    /**
     * Remove all stored edges having the given reference as destination. TODO: also remove the edges not stored but
     * indexed via transitivity
     */
    public void removeRingsTo(DocumentReference object) throws RingException
    {
        XWikiContext context = contextualizer.get();
        XWiki wiki = context.getWiki();
        // TODO: we may use the Solr index instead?
        List<DocumentReference> predecessors = traverser.getDirectPredecessorsViaHql(object);
        for (DocumentReference predecessor : predecessors) {
            XWikiDocument predecessorDocument = factory.getDocument(predecessor, true);
            List<XWikiRing> edges = traverser.getRingsFrom(predecessorDocument);
            List<BaseObject> toBeRemoved = new ArrayList<>();
            for (XWikiRing edge : edges) {
                if (object.equals(edge.getRelatum())) {
                    toBeRemoved.add(edge.getBaseObject());
                }
            }
            for (BaseObject baseObject : toBeRemoved) {
                predecessorDocument.removeXObject(baseObject);
            }
            try {
                wiki.saveDocument(predecessorDocument, "ring.history.remove-edge " + object, true, context);
            } catch (XWikiException e) {
                logger.error("removeRingsTo {} from {}", object, predecessor, e);
                throw new RingException(e);
            }
        }
    }

    public void removeRingsWith(DocumentReference relation) throws RingException
    {
        // TODO: use common code with removeRingsTo, the code only differs by the method to be
        // called on edge: it's edge.getRelation() and edge.getRelatum()
        // TODO: another implementation could consist in using the default relation "IS_CONNECTED_TO" rather
        // than removing the edges completely

        XWikiContext context = contextualizer.get();
        XWiki wiki = context.getWiki();
        List<DocumentReference> predecessors = traverser.getDirectPredecessorsViaHql(relation);
        for (DocumentReference predecessor : predecessors) {
            XWikiDocument predecessorDocument = factory.getDocument(predecessor, true);
            List<XWikiRing> edges = traverser.getRingsFrom(predecessorDocument);
            List<BaseObject> toBeRemoved = new ArrayList<>();
            for (XWikiRing edge : edges) {
                if (relation.equals(edge.getRelation())) {
                    toBeRemoved.add(edge.getBaseObject());
                }
            }
            for (BaseObject baseObject : toBeRemoved) {
                predecessorDocument.removeXObject(baseObject);
            }
            try {
                wiki.saveDocument(predecessorDocument, "ring.history.remove-edge " + relation, true, context);
            } catch (XWikiException e) {
                logger.error("removeRingsTo {} from {}", relation, predecessor, e);
                throw new RingException(e);
            }
        }
    }

    public void removeTerm(DocumentReference identifier) throws RingException
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
            throw new RingException(e);
        }
    }

    public void updateEdge(DocumentReference originVertex, int objectIndex, DocumentReference originalReference,
            DocumentReference newReference, String edgeProperty) throws RingException
    {
        try {
            XWikiContext context = contextualizer.get();
            authorizer.checkAccess(Right.EDIT, context.getUserReference(), originVertex);
            // Clone the document first because its objects will get updated
            XWikiDocument page = context.getWiki().getDocument(originVertex, context).clone();
            logger.debug("Update edge property {} of {} from {} to {}", edgeProperty, originVertex,
                    originalReference, newReference);
            XWikiRing edge = getRing(page, objectIndex);
            if (edgeProperty.equals(Names.HAS_DESTINATION)) {
                edge.setRelatum(newReference);
            } else if (edgeProperty.equals(Names.HAS_RELATION)) {
                edge.setRelation(newReference);
            } else {
                throw new IllegalArgumentException("Illegal edge property name: " + edgeProperty);
            }
            context.getWiki().saveDocument(page, "ring.edge.history.index" + newReference, true,
                    context);
        } catch (AccessDeniedException | XWikiException e) {
            logger.error("updateEdge {} {} {}", originVertex, originalReference, newReference, e);
            throw new RingException(e);
        }
    }

    public void updateRings(DocumentReference term1, DocumentReference term2) throws RingException
    {
        updateRingsTo(term1, term2);
    }

    protected void updateEdges(DocumentReference originalReference, DocumentReference newReference, String edgeProperty)
            throws RingException
    {
        // We could get the BaseObjects directly, so that we don't iterate twice to get the target Edges,
        // just like in #updateEdgesWith
        try {
            String wikiId = originalReference.getWikiReference().getName();
            List<Object[]> entries =
                    traverser.runRingQueryHql(edgeProperty, serializer.serialize(originalReference), wikiId);
            for (Object[] entry : entries) {
                DocumentReference originVertex = resolver.resolve(entry[0].toString(), originalReference);
                int objectIndex = (int) entry[1];
                // TODO: below we consider that edges are stored directly in the vertex document, but actually they could be
                //  stored in their own page with own access rights
                updateEdge(originVertex, objectIndex, originalReference, newReference, edgeProperty);
            }
        } catch (QueryException e) {
            logger.error("updateRingsTo", e);
            throw new RingException(e);
        }
    }

    public void updateRingsTo(DocumentReference originalRelatum, DocumentReference otherRelatum)
            throws RingException
    {
        updateEdges(originalRelatum, otherRelatum, Names.HAS_DESTINATION);
    }

    public void updateEdgesWith(DocumentReference originalRelation, DocumentReference otherRelation)
            throws RingException
    {
        updateEdges(originalRelation, otherRelation, Names.HAS_RELATION);
    }

    @FunctionalInterface
    interface EdgeTargeter
    {
        void setTarget(XWikiRing edge, Object target);
    }
}
