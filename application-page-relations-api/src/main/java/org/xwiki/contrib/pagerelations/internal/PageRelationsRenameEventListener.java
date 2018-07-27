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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.Job;
import org.xwiki.job.JobContext;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

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
public class PageRelationsRenameEventListener extends AbstractEventListener
{
    /**
     * The name of the event listener.
     */
    public static final String LISTENER_NAME = "PageRelationsRenameEventListener";

    private static final EntityReference PAGE_RELATION_CLASS_REFERENCE =
            new LocalDocumentReference(Arrays.asList("PageRelations", "Code"), "PageRelationClass");

    @Inject
    private Logger logger;

    @Inject
    private ObservationContext observationContext;

    @Inject
    private JobContext jobContext;

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> localEntityReferenceSerializer;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

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
        logger.debug("PageRelationRenameListener - Event: [%s] - Source: [%s] - Data: [%s]", event, source, data);

        if (observationContext.isIn(new JobStartedEvent("refactoring/rename"))) {
            Job job = jobContext.getCurrentJob();
            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

            if (references != null && references.size() > 0) {
                XWikiDocument currentDocument = (XWikiDocument) source;
                XWikiContext context = (XWikiContext) data;
                XWiki wiki = context.getWiki();

                String pageField = "page";

                // We admit that we have only one document refactored at the time.
                DocumentReference reference = references.get(0);
                try {

                    Query query = this.queryManager.createQuery(
                        "select distinct doc.fullName from Document doc, "
                            + "doc.object(PageRelations.Code.PageRelationClass) as obj where obj.page=:page",
                        Query.XWQL);
                    String pageName = localEntityReferenceSerializer.serialize(reference);
                    query = query.bindValue(pageField, pageName);
                    String wikiName = currentDocument.getDocumentReference().getWikiReference().getName();
                    query.setWiki(wikiName);

                    List<String> entries = query.execute();
                    for (String inverseRelation : entries) {
                        String fullName = reference.getWikiReference().getName() + ":" + inverseRelation;
                        DocumentReference inverseRelationReference = documentReferenceResolver.resolve(fullName);
                        XWikiDocument inverseRelationDocument = wiki.getDocument(inverseRelationReference, context);

                        BaseObject object = inverseRelationDocument.getXObject(PAGE_RELATION_CLASS_REFERENCE, pageField,
                                pageName, false);

                        if (object != null) {
                            // Note: we should think about serializing the document reference using a more absolute
                            // serializer if we start working with inter-wiki page references.
                            String currentDocumentName =
                                    localEntityReferenceSerializer.serialize(currentDocument.getDocumentReference());

                            object.setStringValue(pageField, currentDocumentName);

                            wiki.saveDocument(inverseRelationDocument,
                                String.format("Update of relation to \"%s\"", currentDocumentName), context);
                        }
                    }
                } catch (XWikiException | QueryException e) {
                    logger.error("Error while updating inverse relations of document [%s].",
                            compactWikiSerializer.serialize(reference), e);
                }
            }
        }

    }
}
