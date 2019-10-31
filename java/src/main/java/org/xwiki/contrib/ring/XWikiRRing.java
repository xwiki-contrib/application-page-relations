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

import aek.ring.RRing;
import aek.ring.RingException;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.doc.XWikiDocument;

@Role
@Unstable
public interface XWikiRRing extends RRing<DocumentReference>
{
    void addRelation(DocumentReference identifier, String label, String domain, String image) throws RingException;

    void addRing(DocumentReference referent, DocumentReference relation, Object relatum) throws RingException;

    void addRing(DocumentReference referent, DocumentReference relatum) throws RingException;

    void addRingOnce(DocumentReference referent, DocumentReference relation, Object relatum) throws RingException;

    void addType(DocumentReference referent, DocumentReference type) throws RingException;

    void addTerm(DocumentReference identifier, String label) throws RingException;

    List<DocumentReference> getInstances(DocumentReference type) throws RingException;

    XWikiRelation getRelation(DocumentReference identifier) throws RingException;

    List<XWikiRelation> getRelations() throws RingException;

    XWikiRing getRing(DocumentReference identifier) throws RingException;

    XWikiRing getRing(XWikiDocument origin, ObjectReference reference) throws RingException;

    XWikiRing getRing(DocumentReference subject, DocumentReference relation, DocumentReference object)
            throws RingException;

    XWikiTerm getTerm(DocumentReference identifier) throws RingException;

    List<DocumentReference> getTypes() throws RingException;

    /**
     * This method uses the Solr index to retrieve all documents having the deleted one as destination because in case
     * of transitive relations, the rings can be present only at the Solr level, not in the SQL database.
     */
    void removeRing(DocumentReference referent, DocumentReference relation, Object relatum) throws RingException;

    /**
     * Remove all rings between origin and destination or destination and origin, whatever the relation is.
     */
    void removeRings(DocumentReference term1, DocumentReference term2) throws RingException;

    void removeRingsFrom(DocumentReference referent) throws RingException;

    void removeRingsTo(DocumentReference object) throws RingException;

    void removeRingsWith(DocumentReference relation) throws RingException;

    void removeRingsWith(DocumentReference referent, DocumentReference relation) throws RingException;

    void removeTerm(DocumentReference identifier) throws RingException;

    void updateRing(DocumentReference referent, int objectIndex, DocumentReference originalReference,
            DocumentReference newReference, String ringProperty) throws RingException;

    void updateRingsTo(DocumentReference originalRelatum, DocumentReference otherRelatum) throws RingException;

    void updateRingsWith(DocumentReference originalRelation, DocumentReference otherRelation) throws RingException;
}
