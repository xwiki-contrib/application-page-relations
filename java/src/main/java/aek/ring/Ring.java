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
package aek.ring;

import org.xwiki.stability.Unstable;

/**
 * A Ring is an ordered set of terms where each term has a well defined role: the first one is the referent,
 * the second one a relation and the third one the relatum. A Ring is a also a Term so that it is possible to
 * involve a Ring in other Rings. In addition a Ring can have properties.
 *
 * @param <I> ringSet and term identifier class
 */
@Unstable
public interface Ring<I> extends Term<I>
{
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
     * Returns the edge, if any. In case a vertex should be connected to several values over the same relation,
     * several edges need to be created: one for each value.
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
     * Returns true if the edge object identifier is not null, false otherwise. See also {@link #hasValue()}. An
     * edge is meant to have either an object identifier or an object value, not both, and at least one of them.
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
