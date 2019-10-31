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
package aek.ring;

import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * A RRing is a set of rings. It can be seen itself as a large ring, a meta ring, a ring of rings.  The structure is a directed 3-uniform hypergraph
 * where rings are edges and also vertices. Edges involved in this hypergraph class link 3 vertices, where each vertex has a specific role: referent,
 * relation and relatum. The 'I' generics represents an identifier interface used to identify terms (vertices), relations and rings (edges).
 *
 * @param <I> Term identifier class
 */
@Unstable
public interface RRing<I>
{
    /**
     * Adds a relation to the RRing with the given identifier, label, domain and image. See also the definition of a relation's domain and image in
     * graphs at <a href="https://en.wikipedia.org/wiki/Binary_relation">Binary relation</a>.
     *
     * @param identifier the relation identifier
     * @param label the relation label
     * @param domain the relation domain, typically a query describing the set of elements it can be applied to
     * @param image the relations image, typically a query describing the set of elements it acceptss
     * @throws RingException in case an error occurs
     */
    void addRelation(I identifier, String label, String domain, String image) throws RingException;

    /**
     * Adds a ring linking the two given terms identified by their identifiers.
     *
     * @param referent referent identifier
     * @param relatum relatum identifier
     * @throws RingException in case an error occurs
     */
    void addRing(I referent, I relatum) throws RingException;

    /**
     * Adds a ring between given referent and the given relatum using the given relation.
     *
     * @param referent identifier of the referent
     * @param relation identifier of the ring relation
     * @param relatum identifier or value of the relatum
     * @throws RingException in case an error occurs
     */
    void addRing(I referent, I relation, Object relatum) throws RingException;

    /**
     * Same as {@link #addRing(Object, Object, Object)} except the ring is created only if an equivalent one does not exist already.
     *
     * @param referent identifier of the referent term
     * @param relation identifier of the ring relation
     * @param relatum relatum identifier or value
     * @throws RingException in case an error occurs
     */
    void addRingOnce(I referent, I relation, Object relatum) throws RingException;

    /**
     * Creates a term with given label and given type.
     *
     * @param identifier term
     * @param label label
     * @param type type
     */
    void addTerm(I identifier, String label, I type) throws RingException;

    /**
     * Adds a type to a term.
     *
     * @param term term identifier
     * @param type type identifier
     * @throws RingException in case an error occurs
     */
    void addType(I term, I type) throws RingException;

    /**
     * Gets all term identifiers having relation "is a" with the given type, i.e. terms which are instances of the given type.
     *
     * @return list of instances of the given type
     */
    List<? extends I> getInstances(I type) throws RingException;

    /**
     * Returns the relation with the given identifier, if it exists in the rring, null otherwise.
     *
     * @param identifier relation identifier
     * @return relation, if found
     */
    Relation<I> getRelation(I identifier) throws RingException;

    /**
     * Returns all the relations that this rring contains.
     *
     * @return list of relations
     */
    List<? extends Relation<I>> getRelations() throws RingException;

    /**
     * Returns the ring corresponding to the given identifier
     *
     * @param identifier ring identifier
     * @return the ring corresponding to the given identifier
     * @throws RingException in case an error occurs
     */
    Ring<I> getRing(I identifier) throws RingException;

    /**
     * Returns the ring between the given subject and object with the given relation, if it exists in the rring, null otherwise.
     *
     * @param referent referent identifier
     * @param relation relation identifier
     * @param relatum relatum term identifier
     * @return found ring, if any
     */
    Ring<I> getRing(I referent, I relation, I relatum) throws RingException;

    /**
     * Returns the term corresponding to the given identifier, if it exists, null otherwise.
     *
     * @param identifier term identifier
     */
    Term<I> getTerm(I identifier) throws RingException;

    /**
     * Returns the list of types in this rring.
     *
     * @return list of types
     * @throws RingException in case an error occurs
     */
    List<? extends I> getTypes() throws RingException;

    /**
     * Removes the ring between the two given vertices. TODO: check what happens when multiple rings
     */
    void removeRing(I referent, I relatum) throws RingException;

    /**
     * Removes the ring corresponding to the given edge referent / relation / relatum, if it exists.
     *
     * @param referent ring referent identifier
     * @param relation ring relation identifier
     * @param relatum ring relatum identifier or value
     */
    void removeRing(I referent, I relation, Object relatum) throws RingException;

    void removeRings(I term) throws RingException;

    /**
     * Removes all edges between termOne and termTwo, whatever the direction and relation.
     *
     * @param term1 edge termOne identifier
     * @param term2 edge termTwo identifier
     */
    void removeRings(I term1, I term2) throws RingException;

    /**
     * Removes all rings having the given term as referent. See also {@link #removeRingsTo(Object)}
     *
     * @param referent term identifier
     */
    void removeRingsFrom(I referent) throws RingException;

    /**
     * Removes all rings having the given term as relatum
     *
     * @param relatum term identifier.
     */
    void removeRingsTo(I relatum) throws RingException;

    /**
     * Remove all rings involving the given relation.
     *
     * @param relation relation identifier
     */
    void removeRingsWith(I relation) throws RingException;

    /**
     * Removes all rings from the given referent and involving the given relation.
     *
     * @param referent a referent
     * @param relation a relation
     */
    void removeRingsWith(I referent, I relation) throws RingException;

    /**
     * Removes the term with the given identifier, if found.
     *
     * @param identifier term identifier
     */
    void removeTerm(I identifier) throws RingException;

    /**
     * Updates all rings from / to the given first term to the second one.
     */
    void updateRings(I term1, I term2) throws RingException;

    /**
     * Updates all rings having the given term as relatum to another term.
     *
     * @param originalRelatum original term identifier
     * @param otherRelatum new term identifier
     */
    void updateRingsTo(I originalRelatum, I otherRelatum) throws RingException;

    /**
     * Updates all edges involving the given relation to another relation.
     *
     * @param originalRelation original relation identifier
     * @param otherRelation other relation identifier
     */
    void updateRingsWith(I originalRelation, I otherRelation) throws RingException;
}
