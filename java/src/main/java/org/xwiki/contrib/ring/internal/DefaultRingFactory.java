/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.xwiki.contrib.ring.XWikiRingFactory;
import org.xwiki.contrib.ring.XWikiRelation;
import org.xwiki.contrib.ring.XWikiTerm;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiRing;
import org.xwiki.contrib.ring.internal.model.BooleanXWikiRing;
import org.xwiki.contrib.ring.internal.model.DateXWikiRing;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiRelation;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiTerm;
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
public class DefaultRingFactory implements XWikiRingFactory
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
                BaseObject baseObject = document.newXObject(DefaultXWikiRing.RING_XCLASS_REFERENCE, context);
                return new DefaultXWikiRing(baseObject, serializer, resolver);
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
            return new DefaultXWikiRing(referent, relation, (DocumentReference) relatum, serializer, resolver);
        } else {
            throw new NotImplementedException("createRing for " + relatum);
        }
    }

    public XWikiRelation createRelation(DocumentReference identifier, String domain, String image,
            boolean transitive)
    {
        return new DefaultXWikiRelation(identifier, domain, image, transitive);
    }

    public XWikiTerm createTerm(DocumentReference identifier)
    {
        return new DefaultXWikiTerm(identifier);
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
            ringClasses.add(new ImmutableTriple<>(DefaultXWikiRing.RING_XCLASS_REFERENCE, DefaultXWikiRing.class,
                    DefaultXWikiRing.class));
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
