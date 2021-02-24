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

    void addTerm(DocumentReference identifier, String label) throws RingException;

    void addTerm(DocumentReference identifier, String label, DocumentReference type) throws RingException;

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
