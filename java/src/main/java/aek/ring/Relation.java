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

import org.xwiki.stability.Unstable;

/**
 * Represents a binary relation that is used by rings / edges to connect a term to another one. A Relation is a Term.
 *
 * @param <I> relation and vertex identifier class
 */
@Unstable
public interface Relation<I> extends Term<I>
{
    /**
     * Returns a representation of the relation image: set of vertices this relation can point at. Typically a query
     * that describes a set of vertices. See also {@link #getDomain()}.
     *
     * @return a representation of the relation image
     */
    String getImage();

    /**
     * Returns a representation of the relation domain: set of vertices this relation can apply to. Typically a query
     * that describes a set of vertices. See also {@link #getImage()}.
     *
     * @return a representation of the relation's domain (set of elements this relation can apply to)
     */
    String getDomain();

    /**
     * Returns true if the relation is transitive: if A R B and B R C, then A R C, false otherwise.
     *
     * @return true if this relation is transitive, false otherwise
     */
    boolean isTransitive();

    /**
     * Sets this relation image.
     *
     * @param image relation image
     */
    void setImage(String image);

    /**
     * Sets this relation domain.
     *
     * @param domain relation domain
     */
    void setDomain(String domain);
}
