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

import java.util.ArrayList;
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
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.EventListener;
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
@Named("PageRelationsRenameEventListener")
@Singleton
public class PageRelationsRenameEventListener implements EventListener
{

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

    @Override
    public String getName()
    {
        return "PageRelationsRenameEventListener";
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.<Event>asList(new DocumentCreatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {

        logger.debug("PageRelationRenameListener - Event:" + event + " - Source: " + source + " - Data: " + data);
        XWikiDocument currentDocument = (XWikiDocument) source;

        boolean isRenameJob = observationContext.isIn(new JobStartedEvent("refactoring/rename"));

        XWikiContext context = (XWikiContext) data;

        XWiki wiki = context.getWiki();

        Job job = jobContext.getCurrentJob();

        String pageField = "page";

        if (isRenameJob) {

            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

            if (references != null && references.size() > 0) {
                DocumentReference reference = references.get(0);
                try {

                    // TODO: make the update generic: update all page fields to the new document
                    // name
                    Query query = this.queryManager.createQuery(
                        "select distinct doc.fullName from Document doc, "
                            + "doc.object(PageRelations.Code.PageRelationClass) as obj where obj.page=:page",
                        Query.XWQL);
                    String name = reference.toString();
                    int idx = name.indexOf(":");
                    if (idx > 0) {
                        name = name.substring(idx + 1);
                    }
                    query = query.bindValue(pageField, name);
                    List entries = query.execute();
                    for (Object entry : entries) {
                        String inverseRelation = entry.toString();
                        XWikiDocument inverseRelationDocument = wiki.getDocument(inverseRelation, context);
                        DocumentReference classReference = getPageRelationClassReference();
                        BaseObject object = inverseRelationDocument.getXObject(classReference, pageField, name);
                        if (object != null) {
                            object.setStringValue(pageField, currentDocument.getFullName());
                            wiki.saveDocument(inverseRelationDocument,
                                "Update of relation to \"" + currentDocument.getFullName() + "\"", context);
                        }
                    }
                } catch (XWikiException | QueryException e) {

                    logger.error("Error while updating inverse relations of document "
                        + compactWikiSerializer.serialize(reference), e);
                }
            }
        }

    }

    // TODO: how to define the reference properly
    protected DocumentReference getPageRelationClassReference()
    {
        ArrayList<String> spaces = new ArrayList<String>();
        spaces.add("PageRelations");
        spaces.add("Code");
        DocumentReference classReference = new DocumentReference("xwiki", spaces, "PageRelationClass");
        return classReference;
    }
}
