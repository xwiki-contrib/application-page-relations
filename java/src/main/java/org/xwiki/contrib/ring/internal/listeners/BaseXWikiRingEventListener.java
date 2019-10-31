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
package org.xwiki.contrib.ring.internal.listeners;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.contrib.ring.XWikiRRing;
import org.xwiki.contrib.ring.XWikiRingIndexer;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

/**
 * Abstract listener inherited by all ringSet event listeners.
 *
 * @version $Id$
 */
public abstract class BaseXWikiRingEventListener extends AbstractEventListener
{
    @Inject
    protected Logger logger;

    @Inject
    @Named("local")
    protected EntityReferenceSerializer<String> serializer;

    @Inject
    @Named("current")
    protected DocumentReferenceResolver<String> resolver;

    @Inject
    protected XWikiRRing ringSet;

    @Inject
    @Named("solr")
    protected XWikiRingIndexer indexer;

    public BaseXWikiRingEventListener(String name, Event... events)
    {
        super(name, events);
    }
}
