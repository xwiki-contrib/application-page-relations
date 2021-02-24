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
package org.xwiki.contrib.ring.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiRingSet;
import org.xwiki.contrib.ring.XWikiRingTraverser;
import org.xwiki.contrib.ring.XWikiRelation;
import org.xwiki.contrib.ring.internal.model.Names;
import io.ring.RingException;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.script.service.ScriptService;

/**
 * Make the Ring API available to scripting.
 *
 * @version $Id$
 */
@Component
@Named("ring")
@Singleton
public class RingScriptService implements ScriptService
{
    @Inject
    protected XWikiRingSet graph;

    @Inject
    @Named("solr-sql")
    protected XWikiRingTraverser traverser;

    public void addRing(DocumentReference origin, DocumentReference relation, DocumentReference destination)
            throws RingException
    {
        graph.addRing(origin, relation, destination);
    }

    public List<DocumentReference> getDirectPredecessors(DocumentReference vertex, DocumentReference relation)
            throws RingException
    {
        return traverser.getDirectPredecessors(vertex, relation);
    }

    public String getDomain(DocumentReference relation) throws RingException
    {
        return graph.getRelation(relation).getDomain();
    }

    public List<XWikiRing> getRingsFrom(DocumentReference identifier) throws RingException
    {
        return traverser.getRingsFrom(identifier);
    }

    public List<XWikiRing> getRingsFrom(DocumentReference identifier, DocumentReference relation)
            throws RingException
    {
        return traverser.getRingsFrom(identifier, relation);
    }

    public String getImage(DocumentReference relation) throws RingException
    {
        return graph.getRelation(relation).getImage();
    }

    public List<DocumentReference> getNeighbours(DocumentReference vertex) throws RingException
    {
        return traverser.getNeighbours(vertex);
    }

    public Query getNeighboursQuery(DocumentReference vertex) throws RingException
    {
        return traverser.getNeighboursQuery(vertex);
    }

    public XWikiRelation getRelation(DocumentReference identifier) throws RingException
    {
        return graph.getRelation(identifier);
    }

    public List<XWikiRelation> getRelations(DocumentReference vertex) throws RingException
    {
        return traverser.getRelations(vertex, getRelations());
    }

    public List<XWikiRelation> getRelations() throws RingException
    {
        return graph.getRelations();
    }

    public void removeRing(DocumentReference referent, DocumentReference relation, DocumentReference relatum)
            throws RingException
    {
        graph.removeRing(referent, relation, relatum);
    }

    public void removeRings(DocumentReference origin, DocumentReference destination) throws RingException
    {
        graph.removeRings(origin, destination);
    }

    public List<DocumentReference> search(String text) throws RingException
    {
        return traverser.search(text);
    }

    public List<DocumentReference> search(String text, XWikiRelation relation) throws RingException
    {
        return traverser.search(text, relation);
    }

    public String getNamespace()
    {
        return Names.RING_NEXUS_CODE_NAMESPACE;
    }

    public String getNamespace(String name)
    {
        if (name.equals("root")) {
            return Names.RING_NEXUS_NAMESPACE;
        }
        return Names.RING_NEXUS_NAMESPACE;
    }
}
