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
import io.ring.RingSet;
import io.ring.RingException;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.doc.XWikiDocument;

/*
  TODO:
    - Rename identifier into reference, denotation?
    - Check access rights, and protection of resources such as Ring:object, and dangerous RingSet methods
    - Add events GraphEvent ringAddedEvent, VertexAddedEvent, ringRemovedEvent, VertexRemovedEvent, ...
      VertexUpdatedEvent, ringUpdatedEvent, ...
    - Check and translate the labels cf xar-handler / ApplicationResources
    - Check with multilingual documents
    - Add a cache of relations
    - The setters of Relation, Ring should be protected to specific users because it allows direct change, and
      such objects are availble from the script service
    - Make it easy to delete Solr index of all pages with an ringSet and recreate it (simple HQL query)
    - When deleting document from Solr index from the XWiki administration via HQL query, it seems the index
      refererring to translated documents are not remove, eg "kuava:XWiki.RingSet.Type_en" remains present in the index
      until we request a deletion from all
    - In the Solr admin console, add ability to enter specify the documents to be removed via a query or a list of
      Solr identifiers
    - When launching Solr reindex, it seems the index is not deleted first
    - When importing a XAR with some pages with language="en", they get imported with no language set, while when
      creating a page, a default language is set.
    - {@link org.xwiki.search.solr.internal.reference.DefaultSolrReferenceResolver#getResover(EntityReference)} should
     be "getResolver" (make the old method deprecated and add a new one?)
    - Add method to EmbeddedSolrInstance to access SolrDocument by id
    - In an existing wiki, check if there are documents with content that have a Solr identifier that does not
      end with a locale id. This could be a bug: Solr identifiers ending with "_" or anything that is not a locale
      identifier should only relate to technical documents (which are supposed to have no language, even though
      they actually have one: the comments in the code use a language, and so do the variable names.
    - It seems Solr delete does not empty completely the index, but only the ones of correct documents that
      can be resolved properly (i.e. from which an EntityReference can be built). If an EntityReference cannot
      get created (which can happen if the index was wrongly created), the wrong index entry will remain.
    - Issue when restoring a deleted vertex: the index is not correctly restored.
    - the property "property.XWiki.RingSet.IsConnectedTo:[PageA]" should remain present in the index until
       there is no ringSet any more invovling PageA.
    - Imagine how a full implementation based on a ringSet database will work: Neo4jGraph, DgraphGraph, etc.
    - Create document, delete it, the Solr index still contains an entry about it
    - Move the SolrConsole to the admin tools?
    - See also
        giraph.apache.org.
            Packages org.apache.giraph.ringSet and org.apache.giraph.ringSet
        jgrapht
    - See org.xwiki.index.tree.internal.nestedpages.ObjectTreeNode / instantion per lookup and inject context etc. However
        we should check that the obtained Term and rings are equal whatever the context is? We don't want one set of
        Vertices, rings, Relations per context.
    - Make sure that the image / domain relations are checked when adding and saving an ringSet
    - Handle other types for ringSet values: Long, Double, etc.
    - User interface to enter / edit scalar values
*/
// * Security aspects: Ring encapsulate a BaseObject which require programming rights. The general idea is to make sure
//         * that 1) the access to these restricted objects is restricted by programming right check, 2) the operations offered by
//         * Ring on these encapsulated objects either are not dangerous or they check the programming rights.

@Role
@Unstable
public interface XWikiRingSet extends RingSet<DocumentReference>
{
    void addRing(DocumentReference referent, DocumentReference relation, Object relatum) throws RingException;

    void addRing(DocumentReference referent, DocumentReference relatum) throws RingException;

    void addRingOnce(DocumentReference referent, DocumentReference relation, Object relatum) throws RingException;

    void addRelation(DocumentReference identifier, String label, String domain, String image) throws RingException;

    void addTerm(DocumentReference identifier, String label) throws RingException;

    void addTerm(DocumentReference identifier, String label, DocumentReference type) throws RingException;

    XWikiRing getRing(DocumentReference identifier) throws RingException;

    XWikiRing getRing(XWikiDocument origin, ObjectReference reference) throws RingException;

    XWikiRing getRing(DocumentReference subject, DocumentReference relation, DocumentReference object)
            throws RingException;

    XWikiRelation getRelation(DocumentReference identifier) throws RingException;

    List<XWikiRelation> getRelations() throws RingException;

    XWikiTerm getTerm(DocumentReference identifier) throws RingException;

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

    void removeTerm(DocumentReference identifier) throws RingException;

    void updateRing(DocumentReference referent, int objectIndex, DocumentReference originalReference,
            DocumentReference newReference, String ringProperty) throws RingException;

    void updateRingsTo(DocumentReference originalRelatum, DocumentReference otherRelatum) throws RingException;

    void updateRingsWith(DocumentReference originalRelation, DocumentReference otherRelation) throws RingException;
}
