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
package org.xwiki.contrib.ring.internal.services;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.search.solr.internal.EmbeddedSolrInstance;

/**
 * RRing Solr instance created to give access to the internal SolrClient:getById method.
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
