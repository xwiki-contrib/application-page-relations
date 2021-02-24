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
package org.xwiki.contrib.graph.internal.listeners;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graph.XWikiEdge;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiEdge;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.XObjectAddedEvent;
import com.xpn.xwiki.internal.event.XObjectDeletedEvent;
import com.xpn.xwiki.internal.event.XObjectEvent;
import com.xpn.xwiki.internal.event.XObjectUpdatedEvent;

/**
 * Listener updating inward page edges when a page gets renamed.
 *
 * @version $Id$
 */
@Component
@Singleton
@Named(XWikiEdgeEventListener.NAME)
public class XWikiEdgeEventListener extends XWikiGraphEventListener
{
    public static final String NAME = "graph.edges";

    public XWikiEdgeEventListener()
    {
        super(NAME, new XObjectAddedEvent(DefaultXWikiEdge.EDGE_OBJECT_REFERENCE),
                new XObjectUpdatedEvent(DefaultXWikiEdge.EDGE_OBJECT_REFERENCE),
                new XObjectDeletedEvent(DefaultXWikiEdge.EDGE_OBJECT_REFERENCE));
    }

    public void onEvent(Event event, Object source, Object data)
    {

        // don't log the context, this can result in endless status.xml
        // TODO: check why when deleting a document entirely, the log is not printed out, while it is on individual
        // object deletion

        if (source != null) {
            XWikiDocument document = (XWikiDocument) source;
            XWikiDocument originalDocument = ((XWikiDocument) source).getOriginalDocument();
            ObjectReference edgeReference = (ObjectReference) ((XObjectEvent) event).getReference();
            try {
                if (event instanceof XObjectAddedEvent) {
                    XWikiEdge edge = graph.getEdge(document, edgeReference);
                    logger.debug("Edge was added: {}", edge);
                    indexer.index(edge);
                } else if (event instanceof XObjectUpdatedEvent) {
                    XWikiEdge edge = graph.getEdge(document, edgeReference);
                    XWikiEdge originalEdge = graph.getEdge(originalDocument, edgeReference);
                    logger.debug("Edge was updated: {}", edge);
                    indexer.unindex(originalEdge);
                    indexer.index(edge);
                } else if (event instanceof XObjectDeletedEvent) {
                    XWikiEdge originalEdge = graph.getEdge(originalDocument, edgeReference);
                    logger.debug("Edge was deleted: {}", originalEdge);
                    indexer.unindex(originalEdge);
                }
            } catch (Exception e) {
                logger.error("onEvent", e);
            }
        }
    }
}
