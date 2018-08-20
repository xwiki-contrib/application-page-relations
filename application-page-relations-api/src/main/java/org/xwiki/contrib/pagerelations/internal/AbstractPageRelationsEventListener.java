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

import org.slf4j.Logger;
import org.xwiki.job.JobContext;
import org.xwiki.localization.ContextualLocalizationManager;
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

/**
 * Abstract listener inherited by all PageRelations listeners.
 *
 * @version $Id$
 */
public abstract class AbstractPageRelationsEventListener extends AbstractEventListener
{
    protected static final EntityReference PAGE_RELATION_CLASS_REFERENCE =
            new LocalDocumentReference(Arrays.asList("PageRelations", "Code"), "PageRelationClass");

    protected static final String PAGE_FIELD = "page";

    @Inject
    protected Logger logger;

    @Inject
    protected ObservationContext observationContext;

    @Inject
    protected JobContext jobContext;

    @Inject
    protected QueryManager queryManager;

    @Inject
    @Named("compactwiki")
    protected EntityReferenceSerializer<String> compactWikiSerializer;

    @Inject
    @Named("local")
    protected EntityReferenceSerializer<String> localEntityReferenceSerializer;

    @Inject
    protected DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    protected ContextualLocalizationManager contextLocalization;

    /**
     * This is the default constructor.
     * @param name Name of the listener
     * @param events Events listened to
     */
    public AbstractPageRelationsEventListener(String name, Event... events)
    {
        super(name, events);
    }

    /**
     * Fetches the inverse relations of a page in the wiki passed as a parameter.
     */
    protected List<String> fetchInverseRelations(String pageName, String wikiName) throws QueryException
    {
        Query query = this.queryManager.createQuery(
                "select distinct doc.fullName from Document doc, "
                        + "doc.object(PageRelations.Code.PageRelationClass) as obj where obj.page=:page",
                Query.XWQL);
        query = query.bindValue(PAGE_FIELD, pageName);
        query.setWiki(wikiName);
        List<String> entries = query.execute();
        return entries;
    }
}
