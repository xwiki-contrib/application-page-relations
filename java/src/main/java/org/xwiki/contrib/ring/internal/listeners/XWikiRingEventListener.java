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
package org.xwiki.contrib.ring.internal.listeners;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.internal.model.DefaultXWikiRing;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.XObjectAddedEvent;
import com.xpn.xwiki.internal.event.XObjectDeletedEvent;
import com.xpn.xwiki.internal.event.XObjectEvent;
import com.xpn.xwiki.internal.event.XObjectUpdatedEvent;

/**
 * Listener updating inward page rings when a page gets renamed.
 *
 * @version $Id$
 */
@Component
@Singleton
@Named(XWikiRingEventListener.NAME)
public class XWikiRingEventListener extends BaseXWikiRingEventListener
{
    public static final String NAME = "ringSet.ringSet";

    public XWikiRingEventListener()
    {
        super(NAME, new XObjectAddedEvent(DefaultXWikiRing.RING_OBJECT_REFERENCE),
                new XObjectUpdatedEvent(DefaultXWikiRing.RING_OBJECT_REFERENCE),
                new XObjectDeletedEvent(DefaultXWikiRing.RING_OBJECT_REFERENCE));
    }

    public void onEvent(Event event, Object source, Object data)
    {

        // don't log the context, this can result in endless status.xml
        // TODO: check why when deleting a document entirely, the log is not printed out, while it is on individual
        // object deletion

        if (source != null) {
            XWikiDocument document = (XWikiDocument) source;
            XWikiDocument originalDocument = ((XWikiDocument) source).getOriginalDocument();
            ObjectReference ringReference = (ObjectReference) ((XObjectEvent) event).getReference();
            try {
                if (event instanceof XObjectAddedEvent) {
                    XWikiRing ring = ringSet.getRing(document, ringReference);
                    logger.debug("Ring was added: {}", ring);
                    indexer.index(ring);
                } else if (event instanceof XObjectUpdatedEvent) {
                    XWikiRing ring = ringSet.getRing(document, ringReference);
                    XWikiRing originalEdge = ringSet.getRing(originalDocument, ringReference);
                    logger.debug("Ring was updated: {}", ring);
                    indexer.unindex(originalEdge);
                    indexer.index(ring);
                } else if (event instanceof XObjectDeletedEvent) {
                    XWikiRing originalRing = ringSet.getRing(originalDocument, ringReference);
                    logger.debug("Ring was deleted: {}", originalRing);
                    indexer.unindex(originalRing);
                }
            } catch (Exception e) {
                logger.error("onEvent", e);
            }
        }
    }
}
