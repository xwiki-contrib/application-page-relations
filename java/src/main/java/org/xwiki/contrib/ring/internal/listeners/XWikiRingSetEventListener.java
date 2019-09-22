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
package org.xwiki.contrib.ring.internal.listeners;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.contrib.ring.XWikiRingSet;
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
public abstract class XWikiRingSetEventListener extends AbstractEventListener
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
    protected XWikiRingSet ringSet;

    @Inject
    @Named("solr")
    protected XWikiRingIndexer indexer;

    public XWikiRingSetEventListener(String name, Event... events)
    {
        super(name, events);
    }
}
