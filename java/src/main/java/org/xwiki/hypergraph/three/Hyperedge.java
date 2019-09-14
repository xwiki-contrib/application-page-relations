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
package org.xwiki.hypergraph.three;

import org.xwiki.hypergraph.two.Edge;
import org.xwiki.hypergraph.Vertex;
import org.xwiki.stability.Unstable;

/**
 * An Hyperedge is an ordered triple consisting of an origin vertex, a relation and a destination vertex or a
 * scalar value. An Hyperedge is a also a Vertex so that it is possible to interlink an edge with other vertices.
 *
 * @param <I> edge and vertex identifier class
 */
@Unstable
public interface Hyperedge<I> extends Edge<I>, Vertex<I>
{
    /**
     * Returns the edge destination identifier, if any. See also {@link #getValue()}.
     *
     * @return edge destination identifier
     */
    I getObject();

    /**
     * Returns the edge origin identifier.
     *
     * @return edge origin identifier
     */
    I getSubject();

    /**
     * Returns the edge relation identifier.
     *
     * @return edge relation identifier
     */
    I getRelation();

    /**
     * Returns the edge's value, if any.
     *
     * @return edge value
     */
    Object getValue();

    /**
     * Returns true if the edge destination identifier is not null, false otherwise. See also {@link #hasValue()}. An
     * edge is meant to have either a destination identifier or an object value, not both.
     *
     * @return true if edge has a non-null destination identifier
     */
    boolean hasObject();

    /**
     * Returns true if the edge relation identifier is not null, false otherwise.
     *
     * @return true if edge has a non-null relation identifier
     */
    boolean hasRelation();

    /**
     * Returns true if the edge value is not null, false otherwise. See also {@link #hasObject()}.
     *
     * @return true if the edge value is not null, false otherwise.
     */
    boolean hasValue();

    /**
     * Sets this edge destination identifier.
     *
     * @param destination vertex identifier
     */
    void setObject(I destination);

    /**
     * Sets this edge relation identifier.
     *
     * @param relation relation identifier
     */
    void setRelation(I relation);

    /**
     * Sets this edge value.
     *
     * @param value edge value
     */
    void setValue(Object value);
}
