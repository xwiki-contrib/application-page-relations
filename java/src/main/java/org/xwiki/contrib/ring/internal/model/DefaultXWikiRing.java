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
package org.xwiki.contrib.ring.internal.model;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.RegexEntityReference;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseObjectReference;

/**
 * Ring implementation.
 *
 * @version $Id$
 */
public class DefaultXWikiRing extends DefaultXWikiTerm implements XWikiRing
{
    /**
     * TODO: this is redundant with the statement document identifier above.
     */
    public static final String RING_TERM_ID = Names.RING_NEXUS_CODE_NAMESPACE + ".RingClass";

    /**
     * Ring object identifier. TODO: check if this is the canonical way TODO: check if there's a way to register object events only for a given class
     * name See also CommentEventGeneratorListener
     */
    public static final RegexEntityReference RING_OBJECT_REFERENCE = BaseObjectReference.any(RING_TERM_ID);

    public static final EntityReference RING_XCLASS_REFERENCE =
            new EntityReference("RingClass", EntityType.DOCUMENT, Names.RING_CODE_SPACE_REFERENCE);

    /**
     * The wrapped BaseObject. Contrarily to Vertices which can be built from a DocumentReference, rings are built entirely from a BaseObject,
     * otherwise we could have Vertices and their rings which do not relate to the same XWikiDocument.
     */
    protected final BaseObject object;

    protected DocumentReferenceResolver<String> resolver;

    protected EntityReferenceSerializer<String> serializer;

    public DefaultXWikiRing(BaseObject object, EntityReferenceSerializer<String> serializer,
            DocumentReferenceResolver<String> resolver)
    {
        // DocumentReference is null because at this stage, only rings attached to an existing document
        // are supported, not yet rings in their own document.
        super(null);
        this.object = object;
        this.serializer = serializer;
        this.resolver = resolver;
    }

    /**
     * This constructor is useful for manipulating rings in memory that are not stored as BaseObjects in XWikiDocuments, but have an existence in the
     * index. Used for instance for computing rings by relation transitivity.
     */
    public DefaultXWikiRing(DocumentReference origin, DocumentReference relation, DocumentReference destination,
            EntityReferenceSerializer<String> serializer, DocumentReferenceResolver<String> resolver)
    {
        super(null);
        object = new BaseObject();
        this.serializer = serializer;
        this.resolver = resolver;
        object.setDocumentReference(origin);
        setRelation(relation);
        setRelatum(destination);
    }

    // TODO: check rights
    public BaseObject getBaseObject()
    {
        return this.object;
    }

    public String getProperties()
    {
        String properties = object.getLargeStringValue(Names.HAS_PROPERTIES);
        if (!StringUtils.isEmpty(properties)) {
            return properties;
        }
        return null;
    }

    public DocumentReference getReferent()
    {
        return object.getDocumentReference();
    }

    public DocumentReference getRelation()
    {
        String relationValue = object.getStringValue(Names.HAS_RELATION);
        if (!StringUtils.isEmpty(relationValue)) {
            return resolver.resolve(relationValue, object.getDocumentReference());
        }
        return null;
    }

    public DocumentReference getRelatum()
    {
        String destinationValue = object.getStringValue(Names.HAS_RELATUM);
        if (!StringUtils.isEmpty(destinationValue)) {
            return resolver.resolve(destinationValue, object.getDocumentReference());
        }

        return null;
    }

    public Object getValue()
    {
        // throw IllegalXxxException?
        return null;
    }

    public Pair<DocumentReference, DocumentReference> getVertices()
    {
        return new MutablePair<>(getReferent(), getRelatum());
    }

    public boolean hasRelation()
    {
        return getRelation() != null;
    }

    public boolean hasRelatum()
    {
        return getRelatum() != null;
    }

    public boolean hasValue()
    {
        return getValue() != null;
    }

    public void setProperties(Object properties)
    {
        if (properties == null || properties.toString() == null) {
            return;
        }

        String str = properties.toString();

        if (StringUtils.isEmpty(str)) {
            return;
        }
        object.setLargeStringValue(Names.HAS_PROPERTIES, str);
    }

    /**
     * Sets a property value.
     *
     * @param propertyName name of the property
     * @param propertyValue value of the property
     */
    protected void setPropertyValue(String propertyName, String propertyValue)
    {
        if (StringUtils.isEmpty(propertyValue)) {
            return;
        }
        object.setStringValue(propertyName, propertyValue);
    }

    public void setRelation(DocumentReference relation)
    {
        setPropertyValue(Names.HAS_RELATION, serializer.serialize(relation));
    }

    public void setRelatum(DocumentReference destination)
    {
        setPropertyValue(Names.HAS_RELATUM, serializer.serialize(destination));
    }

    // TODO: implement equals and hashCode

    public void setValue(Object value)
    {
        // TODO: throw exception?
    }

    public String toString()
    {
        return "referent: " + getReferent() + " - relation: " + getRelation() + " - relatum: " + getRelatum()
                + " value: " + getValue();
    }
}
