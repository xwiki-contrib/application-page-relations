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
package org.xwiki.contrib.graph.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graph.XWikiEdge;
import org.xwiki.contrib.graph.XWikiGraphFactory;
import org.xwiki.contrib.graph.XWikiRelation;
import org.xwiki.contrib.graph.XWikiVertex;
import org.xwiki.contrib.graph.internal.model.BooleanXWikiEdge;
import org.xwiki.contrib.graph.internal.model.DateXWikiEdge;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiEdge;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiRelation;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiVertex;
import org.xwiki.contrib.graph.internal.model.Names;
import org.xwiki.contrib.graph.internal.model.StringXWikiEdge;
import org.xwiki.graph.GraphException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
@Singleton
@Named("default")
public class DefaultGraphFactory implements XWikiGraphFactory
{
    protected List<Triple<EntityReference, Class, Class>> edgeClasses;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextualizer;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    @Inject
    @Named("localization")
    private ScriptService localization;

    // TODO: this method can be called a large number of times, so the class lookup should be optimized (using
    //  a list of Pairs is not).
    public XWikiEdge createEdge(BaseObject object) throws GraphException
    {
        EntityReference classReference = object.getRelativeXClassReference();
        for (Triple<EntityReference, Class, Class> entry : getEdgeClasses()) {
            if (entry.getLeft().equals(classReference)) {
                return createEdge(object, entry.getMiddle());
            }
        }
        return null;
    }

    protected XWikiEdge createEdge(BaseObject object, Class edgeClass) throws GraphException
    {
        try {
            Constructor constructor =
                    edgeClass.getConstructor(BaseObject.class, EntityReferenceSerializer.class,
                            DocumentReferenceResolver.class);
            return (XWikiEdge) constructor.newInstance(object, serializer, resolver);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("createEdge {}", object, e);
            throw new GraphException(e);
        }
    }

    public XWikiEdge createEdge(DocumentReference origin, DocumentReference destination) throws GraphException
    {
        return createEdge(origin, getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME), destination);
    }

    public XWikiEdge createEdge(XWikiDocument document, Object destinationOrValue) throws GraphException
    {

        XWikiContext context = contextualizer.get();
        try {
            // DocumentReference is a special case
            if (destinationOrValue instanceof DocumentReference) {
                BaseObject baseObject = document.newXObject(DefaultXWikiEdge.EDGE_XCLASS_REFERENCE, context);
                return new DefaultXWikiEdge(baseObject, serializer, resolver);
            }

            for (Triple<EntityReference, Class, Class> entry : getEdgeClasses()) {
                if (entry.getRight().equals(destinationOrValue.getClass())) {
                    BaseObject baseObject = document.newXObject(entry.getLeft(), context);
                    return createEdge(baseObject, entry.getMiddle());
                }
            }
        } catch (XWikiException e) {
            logger.error("createEdge {}", document, e);
            throw new GraphException(e);
        }
        return null;
    }

    public XWikiEdge createEdge(DocumentReference origin, DocumentReference relation, Object destination)
    {
        if (destination instanceof DocumentReference) {
            return new DefaultXWikiEdge(origin, relation, (DocumentReference) destination, serializer, resolver);
        } else {
            throw new NotImplementedException("createEdge for " + destination);
        }
    }

    public XWikiRelation createRelation(DocumentReference identifier, String domain, String image,
            boolean transitive)
    {
        return new DefaultXWikiRelation(identifier, domain, image, transitive);
    }

    public XWikiVertex createVertex(DocumentReference identifier)
    {
        return new DefaultXWikiVertex(identifier);
    }

    public XWikiDocument getDocument(DocumentReference vertex, boolean clone) throws GraphException
    {
        if (vertex == null) {
            return null;
        }
        try {
            XWikiContext context = contextualizer.get();
            XWikiDocument document = context.getWiki().getDocument(vertex, context);
            if (clone) {
                return document.clone();
            } else {
                return document;
            }
        } catch (XWikiException e) {
            throw new GraphException(e);
        }
    }

    public List<Triple<EntityReference, Class, Class>> getEdgeClasses()
    {
        if (edgeClasses == null) {
            edgeClasses = new ArrayList<>();
            edgeClasses.add(new ImmutableTriple<>(DefaultXWikiEdge.EDGE_XCLASS_REFERENCE, DefaultXWikiEdge.class,
                    DefaultXWikiEdge.class));
            edgeClasses.add(new ImmutableTriple<>(BooleanXWikiEdge.XCLASS_REFERENCE, BooleanXWikiEdge.class,
                    Boolean.class));
            edgeClasses.add(new ImmutableTriple<>(DateXWikiEdge.XCLASS_REFERENCE, DateXWikiEdge.class, Date.class));
            edgeClasses
                    .add(new ImmutableTriple<>(StringXWikiEdge.XCLASS_REFERENCE, StringXWikiEdge.class, String.class));
        }
        return edgeClasses;
    }

    public DocumentReference getIdentifier(String relativeName)
    {
        XWikiContext context = contextualizer.get();
        WikiReference wikiReference = context.getWikiReference();
        SpaceReference graphSpaceReference = new SpaceReference(Names.GRAPH_SPACE_REFERENCE, wikiReference);
        return new DocumentReference(relativeName, graphSpaceReference);
    }

    public void saveDocument(XWikiDocument page, String message, String... parameters) throws GraphException
    {
        try {
            XWikiContext context = contextualizer.get();
            context.getWiki().saveDocument(page, message, true, context);
        } catch (XWikiException e) {
            logger.error("saveDocument {}", page, e);
            throw new GraphException(e);
        }
    }
}
