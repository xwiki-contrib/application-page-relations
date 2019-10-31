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

/**
 * Factory class used to create terms, relations or rings.
 *
 * @param <I> vertex identifier
 */
public interface RingFactory<I>
{
    /**
     * Creates a Ring with the given referent, relation and relatum. The relatum can either be a term identifier or a
     * scalar value.
     *
     * @param referent ringSet referent
     * @param relation ringSet relation
     * @param relatum ringSet relatum (identifier or scalar)
     * @return the created Ring
     */
    Ring<I> createRing(I referent, I relation, Object relatum) throws RingException;

    /**
     * Creates a Relation with the given identifier, domain, image and transitivity property.
     *
     * @param identifier relation identifier
     * @param domain relation domain
     * @param image relation image
     * @param transitive relation transitivity value
     * @return the created Relation
     */
    Relation<I> createRelation(I identifier, String domain, String image, boolean transitive) throws RingException;
}
