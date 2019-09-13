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
package org.xwiki.contrib.pagerelations.internal;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;
import org.xwiki.query.QueryException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Listener updating inverse page relations when a page gets renamed.
 *
 * @version $Id$
 */
@Component
@Named(PageRelationsRenameEventListener.LISTENER_NAME)
@Singleton
public class PageRelationsRenameEventListener extends AbstractPageRelationsEventListener
{
    /**
     * The name of the event listener.
     */
    public static final String LISTENER_NAME = "pageRelations.listeners.rename";

    /**
     * This is the default constructor.
     */
    public PageRelationsRenameEventListener()
    {
        super(LISTENER_NAME, new DocumentCreatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        logger.debug("[%s] - Event: [%s] - Source: [%s] - Data: [%s]", LISTENER_NAME, event, source, data);

        if (observationContext.isIn(new JobStartedEvent("refactoring/rename"))) {
            Job job = jobContext.getCurrentJob();
            DocumentReference destRef = job.getRequest().getProperty("destination");
            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

            if (references != null && references.size() > 0) {
                XWikiDocument currentDocument = (XWikiDocument) source;
                XWikiContext context = (XWikiContext) data;
                XWiki wiki = context.getWiki();

                DocumentReference sourceRef = references.get(0);
                DocumentReference currentDestRef = currentDocument.getDocumentReference();
                DocumentReference currentSourceRef = getCurrentSourceReference(currentDestRef, sourceRef, destRef);

                // Source and destination for the refactoring of relations under
                DocumentReference reference = currentSourceRef;
                DocumentReference destination = currentDestRef;
                try {

                    String pageName = localEntityReferenceSerializer.serialize(reference);
                    String wikiName = destination.getWikiReference().getName();
                    List<String> entries = fetchInverseRelations(pageName, wikiName);

                    for (String inverseRelation : entries) {
                        String fullName = wikiName + ":" + inverseRelation;
                        DocumentReference inverseRelationReference = documentReferenceResolver.resolve(fullName);
                        XWikiDocument inverseRelationDocument = wiki.getDocument(inverseRelationReference, context);

                        BaseObject object = inverseRelationDocument.getXObject(PAGE_RELATION_CLASS_REFERENCE,
                            PAGE_FIELD, pageName, false);

                        if (object != null) {
                            // Note: we should think about serializing the document reference using a more absolute
                            // serializer if we start working with inter-wiki page references.
                            String currentDocumentName = localEntityReferenceSerializer.serialize(destination);
                            object.setStringValue(PAGE_FIELD, currentDocumentName);

                            String key = "pageRelations.update.page";
                            String message = contextLocalization.getTranslationPlain(key, currentDocumentName);
                            wiki.saveDocument(inverseRelationDocument, message, context);
                        }
                    }
                } catch (XWikiException | QueryException e) {
                    logger.error("Error while updating inverse relations of document [%s].",
                        compactWikiSerializer.serialize(reference), e);
                }
            }
        }
    }

    private DocumentReference getCurrentSourceReference(DocumentReference currentDestRef, DocumentReference sourceRef,
        DocumentReference destRef)
    {
        // compose source and reference of the currently renamed document from the operation's parameters
        if (currentDestRef.equals(destRef)) {
            // the current destination is the exact destination of the global rename operation. current source
            // is the source of the global rename operation.
            return sourceRef;
        } else {
            // the current destination is not the global rename destination, we're in a child of the rename.
            // compose the source from the current destination and global rename references
            // TODO: this code assumes that the sourceRef and destRef are always nested pages (the page is always
            // WebHome) but I guess that this is always true and even if it isn't, I think the currentDestRef is always
            // a page in a subspace
            return currentDestRef.replaceParent(destRef.getParent(), sourceRef.getParent());
        }
    }
}
