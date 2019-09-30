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
import org.xwiki.contrib.ring.XWikiRRing;
import org.xwiki.contrib.ring.XWikiRelation;
import org.xwiki.contrib.ring.XWikiRing;
import org.xwiki.contrib.ring.XWikiRingTraverser;
import org.xwiki.contrib.ring.internal.model.Names;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.script.service.ScriptService;

import aek.ring.RingException;

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
    protected XWikiRRing ring;

    @Inject
    @Named("solr-sql")
    protected XWikiRingTraverser traverser;

    public void addRing(DocumentReference referent, DocumentReference relation, Object relatum)
            throws RingException
    {
        ring.addRing(referent, relation, relatum);
    }

    public List<DocumentReference> getDirectPredecessors(DocumentReference referent, DocumentReference relation)
            throws RingException
    {
        return traverser.getDirectPredecessors(referent, relation);
    }

    public String getDomain(DocumentReference relation) throws RingException
    {
        return ring.getRelation(relation).getDomain();
    }

    public XWikiRing getFirstRingFrom(DocumentReference referent, DocumentReference relation) throws RingException
    {
        return traverser.getFirstRingFrom(referent, relation);
    }

    public String getImage(DocumentReference relation) throws RingException
    {
        return ring.getRelation(relation).getImage();
    }

    public List<DocumentReference> getInstances(DocumentReference type) throws RingException
    {
        return ring.getInstances(type);
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
        return ring.getRelation(identifier);
    }

    public List<XWikiRelation> getRelations(DocumentReference term) throws RingException
    {
        return traverser.getRelations(term, getRelations());
    }

    public List<XWikiRelation> getRelations() throws RingException
    {
        return ring.getRelations();
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

    public List<DocumentReference> getTypes() throws RingException
    {
        return ring.getTypes();
    }

    public void removeRing(DocumentReference referent, DocumentReference relation, DocumentReference relatum)
            throws RingException
    {
        ring.removeRing(referent, relation, relatum);
    }

    public void removeRings(DocumentReference referent, DocumentReference relatum) throws RingException
    {
        ring.removeRings(referent, relatum);
    }

    public void removeRingsWith(DocumentReference referent, DocumentReference relation) throws RingException
    {
        ring.removeRingsWith(referent, relation);
    }

    public List<DocumentReference> search(String text) throws RingException
    {
        return traverser.search(text);
    }

    public List<DocumentReference> search(String text, XWikiRelation relation) throws RingException
    {
        return traverser.search(text, relation);
    }
}
