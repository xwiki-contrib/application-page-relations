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
package org.xwiki.contrib.graph.internal.model;

import org.xwiki.contrib.graph.XWikiEdge;
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
 * Edge implementation.
 *
 * @version $Id$
 */
public class DefaultXWikiEdge extends DefaultXWikiVertex implements XWikiEdge
{
    /**
     * TODO: this is redundant with the statement document identifier above.
     */
    public static final String EDGE_VERTEX_ID = Names.GRAPH_CODE_NAMESPACE + ".EdgeClass";

    /**
     * Edge object identifier. TODO: check if this is the canonical way TODO: check if there's a way to register object
     * events only for a given class name See also CommentEventGeneratorListener
     */
    public static final RegexEntityReference EDGE_OBJECT_REFERENCE = BaseObjectReference.any(EDGE_VERTEX_ID);

    public static final EntityReference EDGE_XCLASS_REFERENCE =
            new EntityReference("EdgeClass", EntityType.DOCUMENT, Names.GRAPH_CODE_SPACE_REFERENCE);

    /**
     * The wrapped BaseObject. Contrarily to Vertices which can be built from a DocumentReference, Edges are built
     * entirely from a BaseObject, otherwise we could have Vertices and their Edges which do not relate to the same
     * XWikiDocument.
     */
    protected final BaseObject object;

    protected DocumentReferenceResolver<String> resolver;

    protected EntityReferenceSerializer<String> serializer;

    public DefaultXWikiEdge(BaseObject object, EntityReferenceSerializer<String> serializer,
            DocumentReferenceResolver<String> resolver)
    {
        // DocumentReference is null because at this stage, only Edges attached to an existing document
        // are supported, not yet edges in their own document.
        super(null);
        this.object = object;
        this.serializer = serializer;
        this.resolver = resolver;
    }

    /**
     * This constructor is useful for manipulating Edges in memory that are not stored as BaseObjects in XWikiDocuments,
     * but have an existence in the index. Used for instance for computing edges by relation transitivity.
     */
    public DefaultXWikiEdge(DocumentReference origin, DocumentReference relation, DocumentReference destination,
            EntityReferenceSerializer<String> serializer, DocumentReferenceResolver<String> resolver)
    {
        super(null);
        object = new BaseObject();
        this.serializer = serializer;
        this.resolver = resolver;
        object.setDocumentReference(origin);
        setRelation(relation);
        setDestination(destination);
    }

    // TODO: check rights
    public BaseObject getBaseObject()
    {
        return this.object;
    }

    public DocumentReference getDestination()
    {
        String destinationValue = object.getStringValue(Names.HAS_DESTINATION);
        if (!StringUtils.isEmpty(destinationValue)) {
            return resolver.resolve(destinationValue, object.getDocumentReference());
        }

        return null;
    }

    public DocumentReference getOrigin()
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

    public Object getValue()
    {
        // throw IllegalXxxException?
        return null;
    }

    public boolean hasDestination()
    {
        return getDestination() != null;
    }

    public boolean hasRelation()
    {
        return getRelation() != null;
    }

    public boolean hasValue()
    {
        return getValue() != null;
    }

    public void setDestination(DocumentReference destination)
    {
        setPropertyValue(Names.HAS_DESTINATION, serializer.serialize(destination));
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

    // TODO: implement equals and hashCode

    public void setValue(Object value)
    {
        // TODO: throw exception?
    }

    public String toString()
    {
        return "origin: " + getOrigin() + " - relation: " + getRelation() + " - destination: " + getDestination()
                + " value: " + getValue();
    }
}
