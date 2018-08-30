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

import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.Job;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.event.Event;
import org.xwiki.query.QueryException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Listener removing inverse page relations when a page gets deleted.
 *
 * @version $Id$
 */
@Component
@Named(PageRelationsDeleteEventListener.LISTENER_NAME)
@Singleton
public class PageRelationsDeleteEventListener extends AbstractPageRelationsEventListener
{
    /**
     * The name of the event listener.
     */
    public static final String LISTENER_NAME = "pageRelations.listeners.delete";

    /**
     * This is the default constructor.
     */
    public PageRelationsDeleteEventListener()
    {
        super(LISTENER_NAME, new DocumentDeletedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        logger.debug("[%s] - Event: [%s] - Source: [%s] - Data: [%s]", LISTENER_NAME, event, source, data);

        if (observationContext.isIn(new JobStartedEvent("refactoring/delete"))) {
            Job job = jobContext.getCurrentJob();

            if (source != null) {
                XWikiDocument currentDocument = (XWikiDocument) source;
                XWikiContext context = (XWikiContext) data;
                XWiki wiki = context.getWiki();
                DocumentReference reference = currentDocument.getDocumentReference();

                try {

                    String pageName = localEntityReferenceSerializer.serialize(reference);
                    String wikiName = reference.getWikiReference().getName();
                    List<String> entries = fetchInverseRelations(pageName, wikiName);

                    for (String inverseRelation : entries) {
                        String fullName = wikiName + ":" + inverseRelation;
                        DocumentReference inverseRelationReference = documentReferenceResolver.resolve(fullName);
                        XWikiDocument inverseRelationDocument = wiki.getDocument(inverseRelationReference, context).clone();

                        BaseObject object =
                                inverseRelationDocument.getXObject(PAGE_RELATION_CLASS_REFERENCE, PAGE_FIELD,
                                        pageName, false);

                        if (object != null) {
                            inverseRelationDocument.removeXObject(object);
                            String key = "pageRelations.remove.history.message";
                            String currentDocumentName =
                                    localEntityReferenceSerializer.serialize(currentDocument.getDocumentReference());
                            String message = contextLocalization.getTranslationPlain(key, currentDocumentName);
                            wiki.saveDocument(inverseRelationDocument, message, context);
                        }
                    }
                } catch (XWikiException | QueryException e) {
                    logger.error("Error while removing inverse relations of document [%s].",
                            compactWikiSerializer.serialize(reference), e);
                }
            }
        }
    }
}
