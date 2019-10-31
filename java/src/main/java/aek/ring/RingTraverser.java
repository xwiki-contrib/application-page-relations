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
 * A RRing traverser.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface RingTraverser<I>
{
    /**
     * TODO: rename to something mentioning "domain": filterRelationsDomain, filterRelationsOnDomain, filterCompatibleRelations
     *
     * @param term a given term identifier
     * @param relations list of existing candidate relations
     * @return the list of relations whose domain is compatible with the given term
     */
    List<? extends Relation<I>> filterRelations(I term, List<? extends Relation<I>> relations) throws RingException;

    /**
     * @return the first edge originating from the given vertex with the given relation
     */
    Ring<I> getFirstRingFrom(I referent, I relation) throws RingException;

    /**
     * @return the list of rings from a given referent toward a given relatum
     */
    List<? extends Ring<I>> getRings(I referent, I relatum) throws RingException;

    /**
     * A wildcard is used so that a list of subclass instances can also be returned.
     *
     * @return the list of edges originating from the given referent
     */
    List<? extends Ring<I>> getRingsFrom(I referent) throws RingException;

    /**
     * @return the list of edges originating from the given referent with the given relation
     */
    List<? extends Ring<I>> getRingsFrom(I referent, I relation) throws RingException;

    /**
     * Returns the rings toward a given relatum via a given relation.
     *
     * @param relatum a term identifier
     * @param relation a relation identifier
     * @return all rings pointing at the  term via the relation
     */
    List<? extends Ring<I>> getRingsTo(I relatum, I relation) throws RingException;

    List<I> search(String text) throws RingException;

    /**
     * @return the given relation's image set filtered by the input text. Example: if relation is "XWiki.RRing.IsA" and the input is empty, the
     * returned vertices will be all types, because the relation's image is "Type". TODO: rename to filterRelationImage or searchInRelationImage
     */
    List<I> search(String text, Relation<I> relation) throws RingException;

    List<I> search(String query, String sort, int max) throws RingException;
}
