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
package org.xwiki.contrib.ring;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.search.solr.internal.metadata.LengthSolrInputDocument;

import com.xpn.xwiki.doc.XWikiDocument;

import aek.ring.Relation;
import aek.ring.RingException;
import aek.ring.RingTraverser;

@Role
public interface XWikiRingTraverser extends RingTraverser<DocumentReference>
{
    List<Object[]> runRingQueryHql(String propertyName, String destinationId, String wikiId)
            throws QueryException;

    /**
     * Get vertices having an ringSet toward a given vertex whose relation matches the given one. TODO: rename to
     * getInboundrings?
     *
     * @param relation a relation
     * @return all vertices pointing at the vertex with the given relation
     */
    List<DocumentReference> getDirectPredecessors(DocumentReference relatum, DocumentReference relation)
            throws RingException;

    /**
     * @param relatum the origin vertex
     * @return list of vertex references which have the origin vertex as destination for at least one ringSet
     * @throws RingException if an error occurs
     */
    List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference relatum) throws RingException;

    /**
     * @param relatum origin vertex
     * @param relation relation
     * @return list of vertices having the origin vertex as destination for at least one ringSet with the given relation
     */
    List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference relatum, DocumentReference relation)
            throws RingException;

    XWikiRing getFirstRingFrom(DocumentReference referent, DocumentReference relation) throws RingException;

    List<DocumentReference> getNeighbours(DocumentReference vertex) throws RingException;

    Query getNeighboursQuery(DocumentReference vertex) throws RingException;

    List<XWikiRing> getRingsFrom(DocumentReference referent) throws RingException;

    List<XWikiRing> getRingsFrom(DocumentReference referent, DocumentReference relation) throws RingException;

    List<XWikiRing> getRingsFrom(XWikiDocument page) throws RingException;

    List<XWikiRing> getRings(DocumentReference referent, DocumentReference relatum) throws RingException;

    /**
     * @return list of relations that are compatible with the vertex, i.e. whose domains contain the vertex
     */
    List<XWikiRelation> getRelations(DocumentReference term, List<? extends Relation<DocumentReference>> relations)
            throws RingException;

    LengthSolrInputDocument getSolrInputDocument(DocumentReference vertex) throws RingException;

    List<DocumentReference> run(Query query) throws RingException;

    List<DocumentReference> search(String query, String sort, int max) throws RingException;

    List<DocumentReference> search(String text) throws RingException;

    List<DocumentReference> search(String text, Relation<DocumentReference> relation) throws RingException;
}
