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
    List<XWikiRelation> filterRelations(DocumentReference term, List<? extends Relation<DocumentReference>> relations) throws RingException;

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
    List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference relatum, DocumentReference relation) throws RingException;

    XWikiRing getFirstRingFrom(DocumentReference referent, DocumentReference relation) throws RingException;

    List<DocumentReference> getNeighbours(DocumentReference vertex) throws RingException;

    Query getNeighboursQuery(DocumentReference vertex) throws RingException;

    List<XWikiRing> getRings(DocumentReference referent, DocumentReference relatum) throws RingException;

    List<XWikiRing> getRingsFrom(DocumentReference referent) throws RingException;

    List<XWikiRing> getRingsFrom(DocumentReference referent, DocumentReference relation) throws RingException;

    List<XWikiRing> getRingsFrom(XWikiDocument page) throws RingException;

    List<XWikiRing> getRingsTo(DocumentReference relatum, DocumentReference relation) throws RingException;

    LengthSolrInputDocument getSolrInputDocument(DocumentReference vertex) throws RingException;

    List<DocumentReference> run(Query query) throws RingException;

    List<Object[]> runRingQueryHql(String propertyName, String destinationId, String wikiId) throws QueryException;

    List<DocumentReference> search(String query, String sort, int max) throws RingException;

    List<DocumentReference> search(String text) throws RingException;

    List<DocumentReference> search(String text, Relation<DocumentReference> relation) throws RingException;
}
