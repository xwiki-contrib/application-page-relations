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
package org.xwiki.contrib.ring.internal.listeners;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRRing;
import org.xwiki.contrib.ring.internal.metadata.XWikiRingSolrMetadataExtractor;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiRing;

import aek.ring.RingException;

import org.xwiki.observation.event.Event;
import org.xwiki.search.solr.internal.SolrIndexEventListener;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AttachmentAddedEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;
import com.xpn.xwiki.internal.event.XObjectAddedEvent;
import com.xpn.xwiki.internal.event.XObjectDeletedEvent;
import com.xpn.xwiki.internal.event.XObjectPropertyAddedEvent;
import com.xpn.xwiki.internal.event.XObjectPropertyDeletedEvent;
import com.xpn.xwiki.internal.event.XObjectPropertyUpdatedEvent;
import com.xpn.xwiki.internal.event.XObjectUpdatedEvent;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Custom listener inheriting from the default one for avoiding reindexing a document's entirely when an ringSet was added,
 * modified or deleted, for performance reasons, since the ringSet index is already managed by {@link
 * XWikiRingSolrMetadataExtractor}. This listener also handles the case where a document is deleted: all rings that were
 * having this document as a relation or as a destination should be removed (other behaviours could be implemented in
 * the future, such as keeping the rings and let them point at an inexistent document, not if it makes sense). As for
 * the rings having this document as origin, they will get removed automatically since they are attached to the document
 * in the current implementation (in the future, rings could be stored in their own page).
 */
@Component
@Singleton
@Named("solr.update")
public class XWikiTermSolrIndexEventListener extends SolrIndexEventListener
{
    private static final List<Event> X_EVENTS = Arrays.<Event>asList(new DocumentUpdatedEvent(),
            new DocumentCreatedEvent(), new DocumentDeletedEvent(), new AttachmentAddedEvent(),
            new AttachmentDeletedEvent(), new AttachmentUpdatedEvent(), new XObjectAddedEvent(),
            new XObjectDeletedEvent(),
            new XObjectUpdatedEvent(), new XObjectPropertyAddedEvent(), new XObjectPropertyDeletedEvent(),
            new XObjectPropertyUpdatedEvent(), new WikiDeletedEvent());

    @Inject
    XWikiRRing ringSet;

    public List<Event> getEvents()
    {
        return X_EVENTS;
    }

    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof DocumentUpdatedEvent) {
            XWikiDocument document = (XWikiDocument) source;
            XWikiDocument originalDocument = document.getOriginalDocument();
            // if document has at least one ringSet and its content has not been updated, don't trigger
            // a reindex of the document itself since the ringSet index is managed already at the level
            // of XWikiRingSolrMetadataExtractor, and the ringSet application does not use the
            // BaseObject index created by the DocumentExtractor.
            List<BaseObject> rings =
                    document.getXObjects(DefaultXWikiRing.RING_OBJECT_REFERENCE);
            if (rings != null && rings.size() > 0) {
                if (!document.getContent().equals(originalDocument.getContent())) {
                    super.onEvent(event, source, data);
                }
            } else {
                super.onEvent(event, source, data);
            }
        } else if (event instanceof DocumentDeletedEvent) {
            XWikiDocument document = (XWikiDocument) source;
            XWikiDocument originalDocument = document.getOriginalDocument();
            try {
                ringSet.removeRingsTo(originalDocument.getDocumentReference());
                ringSet.removeRingsWith(originalDocument.getDocumentReference());
                // NO need to remove the outbound rings at this stage since they will disappear together
                // with the deleted document since no ringSet lives in its own document yet.
            } catch (RingException e) {
                logger.error("DocumentDeletedEvent: {}", originalDocument.getDocumentReference(), e);
            }
        } else {
            super.onEvent(event, source, data);
        }
    }
}
