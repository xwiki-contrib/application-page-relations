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
package org.xwiki.contrib.ring.internal;

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
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiTermFactory;
import org.xwiki.contrib.ring.XWikiRelation;
import org.xwiki.contrib.ring.XWikiTerm;
import org.xwiki.contrib.ring.internal.model.BaseXWikiRing;
import org.xwiki.contrib.ring.internal.model.BooleanXWikiRing;
import org.xwiki.contrib.ring.internal.model.DateXWikiRing;
import org.xwiki.contrib.ring.internal.model.BaseXWikiRelation;
import org.xwiki.contrib.ring.internal.model.BaseXWikiTerm;
import org.xwiki.contrib.ring.internal.model.Names;
import org.xwiki.contrib.ring.internal.model.StringXWikiRing;

import aek.ring.RingException;

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
public class BaseTermFactory implements XWikiTermFactory
{
    protected List<Triple<EntityReference, Class, Class>> ringClasses;

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
    public XWikiRing createRing(BaseObject object) throws RingException
    {
        EntityReference classReference = object.getRelativeXClassReference();
        for (Triple<EntityReference, Class, Class> entry : getRingClasses()) {
            if (entry.getLeft().equals(classReference)) {
                return createRing(object, entry.getMiddle());
            }
        }
        return null;
    }

    protected XWikiRing createRing(BaseObject object, Class ringClass) throws RingException
    {
        try {
            Constructor constructor =
                    ringClass.getConstructor(BaseObject.class, EntityReferenceSerializer.class,
                            DocumentReferenceResolver.class);
            return (XWikiRing) constructor.newInstance(object, serializer, resolver);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("createRing {}", object, e);
            throw new RingException(e);
        }
    }

    public XWikiRing createRing(DocumentReference origin, DocumentReference destination) throws RingException
    {
        return createRing(origin, getIdentifier(Names.IS_CONNECTED_TO_RELATION_NAME), destination);
    }

    public XWikiRing createRing(XWikiDocument document, Object destinationOrValue) throws RingException
    {

        XWikiContext context = contextualizer.get();
        try {
            // DocumentReference is a special case
            if (destinationOrValue instanceof DocumentReference) {
                BaseObject baseObject = document.newXObject(BaseXWikiRing.RING_XCLASS_REFERENCE, context);
                return new BaseXWikiRing(baseObject, serializer, resolver);
            }

            for (Triple<EntityReference, Class, Class> entry : getRingClasses()) {
                if (entry.getRight().equals(destinationOrValue.getClass())) {
                    BaseObject baseObject = document.newXObject(entry.getLeft(), context);
                    return createRing(baseObject, entry.getMiddle());
                }
            }
        } catch (XWikiException e) {
            logger.error("createRing {}", document, e);
            throw new RingException(e);
        }
        return null;
    }

    public XWikiRing createRing(DocumentReference referent, DocumentReference relation, Object relatum)
    {
        if (relatum instanceof DocumentReference) {
            return new BaseXWikiRing(referent, relation, (DocumentReference) relatum, serializer, resolver);
        } else {
            throw new NotImplementedException("createRing for " + relatum);
        }
    }

    public XWikiRelation createRelation(DocumentReference identifier, String domain, String image,
            boolean transitive)
    {
        return new BaseXWikiRelation(identifier, domain, image, transitive);
    }

    public XWikiTerm createTerm(DocumentReference identifier)
    {
        return new BaseXWikiTerm(identifier);
    }

    public XWikiDocument getDocument(DocumentReference vertex, boolean clone) throws RingException
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
            throw new RingException(e);
        }
    }

    public List<Triple<EntityReference, Class, Class>> getRingClasses()
    {
        if (ringClasses == null) {
            ringClasses = new ArrayList<>();
            ringClasses.add(new ImmutableTriple<>(BaseXWikiRing.RING_XCLASS_REFERENCE, BaseXWikiRing.class,
                    BaseXWikiRing.class));
            ringClasses.add(new ImmutableTriple<>(BooleanXWikiRing.XCLASS_REFERENCE, BooleanXWikiRing.class,
                    Boolean.class));
            ringClasses.add(new ImmutableTriple<>(DateXWikiRing.XCLASS_REFERENCE, DateXWikiRing.class, Date.class));
            ringClasses
                    .add(new ImmutableTriple<>(StringXWikiRing.XCLASS_REFERENCE, StringXWikiRing.class, String.class));
        }
        return ringClasses;
    }

    public DocumentReference getIdentifier(String relativeName)
    {
        XWikiContext context = contextualizer.get();
        WikiReference wikiReference = context.getWikiReference();
        SpaceReference graphSpaceReference = new SpaceReference(Names.RING_NEXUS_SPACE_REFERENCE, wikiReference);
        return new DocumentReference(relativeName, graphSpaceReference);
    }

    public void saveDocument(XWikiDocument page, String message, String... parameters) throws RingException
    {
        try {
            XWikiContext context = contextualizer.get();
            context.getWiki().saveDocument(page, message, true, context);
        } catch (XWikiException e) {
            logger.error("saveDocument {}", page, e);
            throw new RingException(e);
        }
    }
}
