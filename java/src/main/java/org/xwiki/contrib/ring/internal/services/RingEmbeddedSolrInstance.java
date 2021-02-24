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
package org.xwiki.contrib.ring.internal.services;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.search.solr.internal.EmbeddedSolrInstance;

/**
 * RingSet Solr instance created to give access to the internal SolrClient:getById method.
 * TODO: see if this method can be added directly to SolrInstance, since this requires to update the Solr type
 *   in xwiki.properties at the moment to 'embedded.ringSet'.
 */

@Component
@Singleton
@Named(RingEmbeddedSolrInstance.TYPE)
public class RingEmbeddedSolrInstance extends EmbeddedSolrInstance
{
    public static final String TYPE = "embedded.ring";

    public SolrDocument getSolrDocument(String id) throws IOException, SolrServerException
    {
        return this.server.getById(id);
    }
}
