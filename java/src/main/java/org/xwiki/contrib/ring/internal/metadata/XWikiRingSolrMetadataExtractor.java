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
package org.xwiki.contrib.ring.internal.metadata;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.common.SolrInputDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiRingIndexer;
import org.xwiki.contrib.ring.XWikiRingFactory;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiRing;
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
    @Named("solr")
    private XWikiRingIndexer indexer;

    @Inject
    private XWikiRingFactory factory;

    /**
     * Overrides in order to index Ring objects in a specific manner, and to compute the identifier via the ringSet
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
        if (relativeXClassReference.equals(DefaultXWikiRing.RING_XCLASS_REFERENCE)) {
            XWikiRing ring = factory.createRing(object);
            indexer.index(ring);
        }

        return true;
    }

    // TODO: see if there's a need to override setLocaleAndContentFields
}
