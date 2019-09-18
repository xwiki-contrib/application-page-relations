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

import org.xwiki.stability.Unstable;

/**
 * Graph indexer that maintains an index of Hyperedges, Vertices and Relations for easing graph traversal.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface GraphIndexer<I>
{
    /**
     * Adds the given edge to the index.
     *
     * @param edge the edge identifier to index
     * @throws GraphException in case an error occurs
     */
    void index(Edge<I> edge) throws GraphException;

    /**
     * Removes the given edge from the index.
     *
     * @param edge the edge identifier to be removed
     * @throws GraphException in case an error occurs
     */
    void unindex(Edge<I> edge) throws GraphException;
}
