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
 * A Ring is an ordered set of terms where each term has a well defined role: the first one is the referent, the second one a relation and the third
 * one is either a relatum with an identifier, or a scalar value. A Ring is a also a Term so that it is possible to involve a Ring in other Rings. In
 * addition, a Ring can have properties.
 *
 * @param <I> term identifier class
 */
@Unstable
public interface Ring<I> extends Term<I>
{
    /**
     * Returns an object storing properties of that ring.
     *
     * @return properties
     */
    Object getProperties();

    /**
     * Returns the ring referent identifier.
     *
     * @return referent identifier
     */
    I getReferent();

    /**
     * Returns the ring relation identifier.
     *
     * @return relation identifier
     */
    I getRelation();

    /**
     * Returns the ring relatum identifier, if any. See also {@link #getValue()}.
     *
     * @return edge object identifier
     */
    I getRelatum();

    /**
     * Returns the edge, if any. In case a vertex should be connected to several values over the same relation, several edges need to be created: one
     * for each value.
     *
     * @return edge value
     */
    Object getValue();

    /**
     * Returns true if the edge relation identifier is not null, false otherwise.
     *
     * @return true if edge has a non-null relation identifier
     */
    boolean hasRelation();

    /**
     * Returns true if the edge object identifier is not null, false otherwise. See also {@link #hasValue()}. An edge is meant to have either an
     * object identifier or an object value, not both, and at least one of them.
     *
     * @return true if edge has a non-null object identifier
     */
    boolean hasRelatum();

    /**
     * Returns true if the edge value is not null, false otherwise. See also {@link #hasRelatum()}.
     *
     * @return true if the edge value is not null, false otherwise.
     */
    boolean hasValue();

    /**
     * Sets this ring properties.
     */
    void setProperties(Object properties);

    /**
     * Sets this edge relation identifier.
     *
     * @param relation relation identifier
     */
    void setRelation(I relation);

    /**
     * Sets this edge object identifier.
     *
     * @param object vertex identifier
     */
    void setRelatum(I object);

    /**
     * Sets this edge value.
     *
     * @param value edge value
     */
    void setValue(Object value);
}
