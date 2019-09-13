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
import org.xwiki.contrib.graph.XWikiGraph;
import org.xwiki.contrib.graph.internal.model.DefaultXWikiEdge;
import org.xwiki.contrib.graph.internal.metadata.XWikiEdgeSolrMetadataExtractor;
import org.xwiki.graph.GraphException;
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
 * Custom listener inheriting from the default one for avoiding reindexing a document's entirely when an edge was added,
 * modified or deleted, for performance reasons, since the edge index is already managed by {@link
 * XWikiEdgeSolrMetadataExtractor}. This listener also handles the case where a document is deleted: all edges that were
 * having this document as a relation or as a destination should be removed (other behaviours could be implemented in
 * the future, such as keeping the edges and let them point at an inexistent document, not if it makes sense). As for
 * the edges having this document as origin, they will get removed automatically since they are attached to the document
 * in the current implementation (in the future, edges could be stored in their own page).
 */
@Component
@Singleton
@Named("solr.update")
public class GraphSolrIndexEventListener extends SolrIndexEventListener
{
    private static final List<Event> X_EVENTS = Arrays.<Event>asList(new DocumentUpdatedEvent(),
            new DocumentCreatedEvent(), new DocumentDeletedEvent(), new AttachmentAddedEvent(),
            new AttachmentDeletedEvent(), new AttachmentUpdatedEvent(), new XObjectAddedEvent(),
            new XObjectDeletedEvent(),
            new XObjectUpdatedEvent(), new XObjectPropertyAddedEvent(), new XObjectPropertyDeletedEvent(),
            new XObjectPropertyUpdatedEvent(), new WikiDeletedEvent());

    @Inject
    XWikiGraph graph;

    public List<Event> getEvents()
    {
        return X_EVENTS;
    }

    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof DocumentUpdatedEvent) {
            XWikiDocument document = (XWikiDocument) source;
            XWikiDocument originalDocument = document.getOriginalDocument();
            // if document has at least one edge and its content has not been updated, don't trigger
            // a reindex of the document itself since the edge index is managed already at the level
            // of XWikiEdgeSolrMetadataExtractor, and the graph application does not use the
            // BaseObject index created by the DocumentExtractor.
            List<BaseObject> edges =
                    document.getXObjects(DefaultXWikiEdge.EDGE_OBJECT_REFERENCE);
            if (edges != null && edges.size() > 0) {
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
                graph.removeEdgesTo(originalDocument.getDocumentReference());
                graph.removeEdgesWith(originalDocument.getDocumentReference());
                // NO need to remove the outbound edges at this stage since they will disappear together
                // with the deleted document since no edge lives in its own document yet.
            } catch (GraphException e) {
                logger.error("DocumentDeletedEvent: {}", originalDocument.getDocumentReference(), e);
            }
        } else {
            super.onEvent(event, source, data);
        }
    }
}
