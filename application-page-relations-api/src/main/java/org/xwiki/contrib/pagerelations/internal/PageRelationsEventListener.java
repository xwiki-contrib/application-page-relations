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
import org.xwiki.contrib.pagerelations.PageRelationsQueryExecutor;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;
import org.xwiki.query.QueryException;
import org.xwiki.refactoring.event.DocumentRenamedEvent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Listener updating inverse page relations when a page gets renamed or deleted.
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
    protected PageRelationsQueryExecutor pageRelations;

    @Inject
    protected ObservationContext observationContext;

    /**
     * This is the default constructor.
     */
    public PageRelationsEventListener()
    {
        super(LISTENER_NAME, new DocumentRenamedEvent(), new DocumentDeletedEvent());
    }

    @Override
    // TODO: create function in PageRelationService for computing the "page" field value as a relative or absolute id
    //  depending whether the two pages are in the same wiki or not (and use it also from Velocity).
    public void onEvent(Event event, Object source, Object data)
    {
        logger.debug("[%s] - Event: [%s] - Source: [%s] - Data: [%s]", LISTENER_NAME, event, source, data);

        DocumentReference complement = null;

        if (event instanceof DocumentRenamedEvent) {
            complement = ((DocumentRenamedEvent) event).getSourceReference();
        } else if (event instanceof DocumentDeletedEvent) {
            boolean isRename = observationContext.isIn(new JobStartedEvent("refactoring/rename"));
            if (isRename) {
                return;
            }
            complement = ((XWikiDocument) source).getDocumentReference();
        }
        try {
            List<DocumentReference> incomingRelations = pageRelations.getIncomingRelations(complement);
            logger.debug("Inverse relations of [{}]: [{}].", defaultSerializer.serialize(complement),
                incomingRelations);
            for (DocumentReference subject : incomingRelations) {
                if (event instanceof DocumentRenamedEvent) {
                    DocumentReference newComplement = ((DocumentRenamedEvent) event).getTargetReference();
                    this.updateRelation(subject, complement, newComplement);
                } else if (event instanceof DocumentDeletedEvent) {
                    this.deleteRelation(subject, complement);
                }
            }
        } catch (QueryException | XWikiException e) {
            e.printStackTrace();
            logger.error("Error while updating inverse relations of document [{}].",
                defaultSerializer.serialize(complement), e);
        }
    }

    /**
     * Updates the PageRelation XObject matching the given subject and complement to a new complement.
     *
     * @param subject Relation's subject reference
     * @param originalComplement Relation's original complement reference
     * @param newComplement Relation's new complement reference
     * @throws XWikiException Raised in case of error
     */
    public void updateRelation(DocumentReference subject, DocumentReference originalComplement,
        DocumentReference newComplement) throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();
        XWikiDocument relatedPage = wiki.getDocument(subject, context).clone();
        String relationObjectValue = compactWikiSerializer.serialize(originalComplement, subject);
        BaseObject relationObject =
            relatedPage.getXObject(PageRelationsQueryExecutor.PAGE_RELATION_CLASS_REFERENCE,
                PageRelationsQueryExecutor.PAGE_FIELD, relationObjectValue, false);
        if (relationObject != null) {
            logger.debug("Updating inverse relation: [{}].", subject);
            String newRelationObjectValue = compactWikiSerializer.serialize(newComplement, subject);
            relationObject.setStringValue(PageRelationsQueryExecutor.PAGE_FIELD, newRelationObjectValue);
            String newPageId = defaultSerializer.serialize(newComplement);
            String message = contextLocalization.getTranslationPlain("pageRelations.update.page", newPageId);
            wiki.saveDocument(relatedPage, message, context);
        }
    }

    /**
     * Deletes the first PageRelation XObject found matching the given subject and complement.
     * @param subject Relation's subject reference
     * @param complement Relation's complement reference
     * @throws XWikiException Raised in case an error occurs
     */
    public void deleteRelation(DocumentReference subject, DocumentReference complement)
        throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();
        XWikiDocument relatedPage = wiki.getDocument(subject, context).clone();
        String relationObjectValue = compactWikiSerializer.serialize(complement, subject);
        BaseObject relationObject =
            relatedPage.getXObject(PageRelationsQueryExecutor.PAGE_RELATION_CLASS_REFERENCE,
                PageRelationsQueryExecutor.PAGE_FIELD, relationObjectValue, false);
        if (relationObject != null) {
            logger.debug("Deleting inverse relation: [{}].", subject);
            String message = contextLocalization.getTranslationPlain("pageRelations.remove.page", subject);
            relatedPage.removeXObject(relationObject);
            wiki.saveDocument(relatedPage, message, context);
        }
    }
}
