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
package org.xwiki.contrib.ring.internal.metadata;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.common.SolrInputDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiRingSet;
import org.xwiki.contrib.ring.XWikiRingIndexer;
import org.xwiki.contrib.ring.XWikiTermFactory;
import org.xwiki.contrib.ring.internal.model.BaseXWikiRing;
import org.xwiki.contrib.ring.internal.services.SolrRingIndexer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.search.solr.internal.api.FieldUtils;
import org.xwiki.search.solr.internal.metadata.LengthSolrInputDocument;
import org.xwiki.search.solr.internal.metadata.ObjectSolrMetadataExtractor;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseObjectReference;

/**
 * Extractor with higher priority than ObjectSolrMetadataExtractor in order to implement a custom indexation of Ring
 * objects.
 */
@Component
@Named("object")
@Singleton
public class XWikiRingSolrMetadataExtractor extends ObjectSolrMetadataExtractor
{
    @Inject
    private XWikiRingSet graph;

    @Inject
    @Named("solr")
    private XWikiRingIndexer indexer;

    @Inject
    private XWikiTermFactory factory;

    /**
     * Overrides in order to index Ring objects in a specific manner, and to compute the identifier via the ring
     * service, in order to make sure it contains a locale and to call a customized version of {@link
     * #setDocumentFields(DocumentReference, SolrInputDocument)} so that the fields get set even if the document does
     * not exist yet. See {@link SolrRingIndexer#getSolrIdentifier(EntityReference)}
     */
    public boolean setFieldsInternal(LengthSolrInputDocument solrDocument, EntityReference entityReference)
            throws Exception
    {
        BaseObjectReference objectReference = new BaseObjectReference(entityReference);

        DocumentReference classReference = objectReference.getXClassReference();
        DocumentReference documentReference = new DocumentReference(objectReference.getParent());

        XWikiDocument originalDocument = getDocument(documentReference);
        BaseObject object = originalDocument.getXObject(objectReference);
        if (object == null) {
            return false;
        }

        solrDocument
                .setField(FieldUtils.ID, ((SolrRingIndexer) indexer).getSolrIdentifier(object.getReference()));
        ((SolrRingIndexer) indexer).setDocumentFields(documentReference, solrDocument);
        solrDocument.setField(FieldUtils.TYPE, objectReference.getType().name());
        solrDocument.setField(FieldUtils.CLASS, localSerializer.serialize(classReference));
        solrDocument.setField(FieldUtils.NUMBER, objectReference.getObjectNumber());

        setLocaleAndContentFields(documentReference, solrDocument, object);

        EntityReference relativeXClassReference = object.getRelativeXClassReference();
        if (relativeXClassReference.equals(BaseXWikiRing.EDGE_XCLASS_REFERENCE)) {
            XWikiRing edge = factory.createEdge(object);
            indexer.index(edge);
        }

        return true;
    }

    // TODO: see if there's a need to override setLocaleAndContentFields
}
