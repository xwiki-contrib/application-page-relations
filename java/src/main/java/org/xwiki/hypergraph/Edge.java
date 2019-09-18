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
package org.xwiki.hypergraph;

import org.xwiki.hypergraph.Vertex;
import org.xwiki.stability.Unstable;

/**
 * An Edge is an ordered triple of vertices where each vertex has a well defined role: the first one is a subject,
 * the second one a relation and the third one an object. An Edge is a also a Vertex so that it is possible to
 * involve an Edge in other Hyperedges.
 *
 * @param <I> edge and vertex identifier class
 */
@Unstable
public interface Edge<I> extends Vertex<I>
{
    /**
     * Returns the edge object identifier, if any. See also {@link #getValue()}.
     *
     * @return edge object identifier
     */
    I getObject();

    /**
     * Returns the edge subject identifier.
     *
     * @return edge subject identifier
     */
    I getSubject();

    /**
     * Returns the edge relation identifier.
     *
     * @return edge relation identifier
     */
    I getRelation();

    /**
     * Returns the edge, if any. In case a vertex should be connected to several values over the same relation,
     * several edges need to be created: one for each value.
     *
     * @return edge value
     */
    Object getValue();

    /**
     * Returns true if the edge object identifier is not null, false otherwise. See also {@link #hasValue()}. An
     * edge is meant to have either an object identifier or an object value, not both, and at least one of them.
     *
     * @return true if edge has a non-null object identifier
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
     * Sets this edge object identifier.
     *
     * @param object vertex identifier
     */
    void setObject(I object);

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
