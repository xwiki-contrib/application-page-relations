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
package io.ring;

import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * Represents a set of rings. Can be seen itself as a large ring, a meta ring, a ring of rings.  The structure is a
 * directed 3-uniform hypergraph where rings are edges, and are also vertices. Rings involved in this hypergraph class
 * link 3 vertices, where each vertex has a specific role: referent, relation and relatum. The "I" generics represents
 * an identifier interface used to identify terms (vertices), relations and rings (edges).
 *
 * @param <I> term identifier class
 */
@Unstable
public interface RingSet<I>
{
    /**
     * Adds a relation to the ring with the given identifier, label, domain and image. See also the definition of a
     * relation's domain and image in graphs at <a href="https://en.wikipedia.org/wiki/Binary_relation">Binary
     * relation</a>.
     *
     * @param identifier the relation identifier
     * @param label the relation label
     * @param domain the relation domain, typically a query describing the set of elements it can be applied to
     * @param image the relations image, typically a query describing the set of elements it acceptss
     * @throws RingException in case an error occurs
     */
    void addRelation(I identifier, String label, String domain, String image) throws RingException;

    /**
     * Adds edge between the two given vertex identifiers.
     *
     * @param referent edge vertex one
     * @param relatum edge vertex two
     * @throws RingException in case an error occurs
     */
    void addRing(I referent, I relatum) throws RingException;

    /**
     * Adds an edge to the ring from between given referent and the given relatum using the given relation.
     *
     * @param referent identifier of the subjectvertex
     * @param relation identifier of the edge's relation
     * @param relatum identifier or value of the relatum
     * @throws RingException in case an error occurs
     */
    void addRing(I referent, I relation, Object relatum) throws RingException;

    /**
     * Same as {@link #addRing(Object, Object, Object)} except the edge is created only if an equivalent one does not
     * exist already.
     *
     * @param referent identifier of the referent vertex
     * @param relation identifier of the edge relation
     * @param relatum relatum identifier or value of the edge
     * @throws RingException in case an error occurs
     */
    void addRingOnce(I referent, I relation, Object relatum) throws RingException;

    /**
     * Adds a vertex to this ring.
     *
     * @param identifier vertex identifier
     */
    void addTerm(I identifier) throws RingException;

    /**
     * Adds a vertex to the ring with the given identifier and label.
     *
     * @param identifier vertex identifier
     * @param label vertex label
     * @throws RingException in case an error occurs
     */
    void addTerm(I identifier, String label) throws RingException;

    /**
     * Returns the relation with the given identifier, if it exists in the ring, null otherwise.
     *
     * @param identifier relation identifier
     * @return relation, if found
     */
    Relation<I> getRelation(I identifier) throws RingException;

    /**
     * Returns all the relations that this ring contains.
     *
     * @return list of relations
     */
    List<? extends Relation<I>> getRelations() throws RingException;

    /**
     * Returns the edge corresponding to the given identifier
     *
     * @param identifier eddge identifier
     * @return the edge corresponding to the given identifier
     * @throws RingException in case an error occurs
     */
    Ring<I> getRing(I identifier) throws RingException;

    /**
     * Returns the edge between the given subject and object with the given relation, if it exists in the ring, null
     * otherwise.
     *
     * @param subject subject vertex identifier
     * @param relation edge relation identifier
     * @param object object vertex identifier
     * @return found edge, if any
     */
    Ring<I> getRing(I subject, I relation, I object) throws RingException;

    /**
     * Returns the vertex corresponding to the given identifier, if it exists, null otherwise.
     *
     * @param identifier vertex identifier
     */
    Term<I> getTerm(I identifier) throws RingException;

    /**
     * Remove all edges involving the given relation.
     *
     * @param relation relation identifier
     */
    void removeRingsWith(I relation) throws RingException;

    /**
     * Remove the edge between the two given vertices. TODO: check what happens when multiple rings
     */
    void removeRing(I referent, I relatum) throws RingException;

    /**
     * Removes the edge corresponding to the given edge referent / relation / relatum, if it exists.
     *
     * @param referent edge referent identifier
     * @param relation edge relation identifier
     * @param relatum edge relatum identifier or edge value
     */
    void removeRing(I referent, I relation, Object relatum) throws RingException;

    void removeRings(I vertex) throws RingException;

    /**
     * Removes all edges between termOne and termTwo, whatever the direction and relation.
     *
     * @param termOne edge termOne identifier
     * @param termTwo edge termTwo identifier
     */
    void removeRings(I termOne, I termTwo) throws RingException;

    /**
     * Removes all edges having the given vertex as referent. See also {@link #removeRingsTo(Object)}
     *
     * @param referent vertex identifier
     */
    void removeRingsFrom(I referent) throws RingException;

    /**
     * Removes all edges having the given vertex as object
     *
     * @param object vertex identifier.
     */
    void removeRingsTo(I object) throws RingException;

    /**
     * Removes the vertex with the given identifier, if found.
     *
     * @param identifier vertex identifier
     */
    void removeTerm(I identifier) throws RingException;

    /**
     * Updates all edges from / to the given first vertex to the second one.
     */
    void updateRings(I term1, I term2) throws RingException;

    /**
     * Updates all edges having the given vertex as object to another vertex.
     *
     * @param originalRelatum original vertex identifier
     * @param otherRelatum new vertex identifier
     */
    void updateRingsTo(I originalRelatum, I otherRelatum) throws RingException;

    /**
     * Updates all edges involving the given relation to another relation.
     *
     * @param originalRelation original relation identifier
     * @param otherRelation other relation identifier
     */
    void updateEdgesWith(I originalRelation, I otherRelation) throws RingException;
}
