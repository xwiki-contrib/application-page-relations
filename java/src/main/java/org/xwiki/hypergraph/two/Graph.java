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
package org.xwiki.hypergraph.two;

import org.xwiki.hypergraph.GraphException;
import org.xwiki.hypergraph.Vertex;

public interface Graph<I>
{
    void addEdge(I vertex1, I vertex2) throws GraphException;

    void addVertex(I identifier) throws GraphException;

    /**
     * Returns the vertex corresponding to the given identifier, if it exists, null otherwise.
     *
     * @param identifier vertex identifier
     */
    Vertex<I> getVertex(I identifier) throws GraphException;

    void removeEdge(I vertex1, I vertex2) throws GraphException;

    void removeEdges(I vertex) throws GraphException;

    /**
     * Removes the vertex with the given identifier, if found.
     *
     * @param identifier vertex identifier
     */
    void removeVertex(I identifier) throws GraphException;

    void updateEdges(I vertex1, I vertex2) throws GraphException;
}
