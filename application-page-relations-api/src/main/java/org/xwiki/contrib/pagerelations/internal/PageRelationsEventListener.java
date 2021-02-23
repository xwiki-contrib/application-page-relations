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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.pagerelations.PageRelationsService;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.query.QueryException;
import org.xwiki.refactoring.event.DocumentRenamingEvent;

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
@Named(PageRelationsEventListener.LISTENER_NAME)
@Singleton
public class PageRelationsEventListener extends AbstractEventListener
{
    /**
     * The name of the event listener.
     */
    public static final String LISTENER_NAME = "pageRelations.listener";

    @Inject
    protected Logger logger;

    @Inject
    @Named("compactwiki")
    protected EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    @Named("default")
    protected EntityReferenceSerializer<String> defaultSerializer;

    @Inject
    protected Provider<XWikiContext> contextProvider;

    @Inject
    protected ContextualLocalizationManager contextLocalization;

    @Inject
    protected PageRelationsService pageRelations;

    /**
     * This is the default constructor.
     */
    public PageRelationsEventListener()
    {
        super(LISTENER_NAME, new DocumentRenamingEvent(), new DocumentDeletedEvent());
    }

    @Override
    // TODO: create function in PageRelationService for computing the "page" field value as a relative or absolute id
    //  depending whether the two pages are in the same wiki or not (and use it also from Velocity).
    public void onEvent(Event event, Object source, Object data)
    {
        logger.debug("[%s] - Event: [%s] - Source: [%s] - Data: [%s]", LISTENER_NAME, event, source, data);

        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();
        DocumentReference originalReference = null;

        if (event instanceof DocumentRenamingEvent) {
            originalReference = ((DocumentRenamingEvent) event).getSourceReference();
        } else if (event instanceof DocumentDeletedEvent) {
            originalReference = ((XWikiDocument) source).getDocumentReference();
        }
//        String originalWikiId = originalReference.getWikiReference().getName();
//        String originalLocalPageId = compactWikiSerializer.serialize(originalReference, originalReference);

        try {
            List<DocumentReference> incomingRelations = pageRelations.getIncomingRelations(originalReference);
            logger.debug("Inverse relations of [{}]: [{}].", defaultSerializer.serialize(originalReference),
                incomingRelations);
            for (DocumentReference reference : incomingRelations) {
                XWikiDocument relatedPage = wiki.getDocument(reference, context);
                String relationObjectValue = compactWikiSerializer.serialize(originalReference, reference);
                BaseObject relationObject = relatedPage.getXObject(PageRelationsService.PAGE_RELATION_CLASS_REFERENCE,
                    PageRelationsService.PAGE_FIELD, relationObjectValue, false);
                if (relationObject != null) {
                    String message = null;
                    if (event instanceof DocumentRenamingEvent) {
                        DocumentReference newReference = ((DocumentRenamingEvent) event).getTargetReference();
                        relationObjectValue = compactWikiSerializer.serialize(newReference, reference);
                        relationObject.setStringValue(PageRelationsService.PAGE_FIELD, relationObjectValue);
                        String newPageId = defaultSerializer.serialize(newReference);
                        message = contextLocalization.getTranslationPlain("pageRelations.update.page", newPageId);
                    } else if (event instanceof DocumentDeletedEvent) {
                        relatedPage.removeXObject(relationObject);
                    }
                    wiki.saveDocument(relatedPage, message, context);
                }
            }
        } catch (QueryException | XWikiException e) {
            e.printStackTrace();
            logger.error("Error while updating inverse relations of document [{}].",
                defaultSerializer.serialize(originalReference), e);
        }
    }
}
