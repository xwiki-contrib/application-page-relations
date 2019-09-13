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
package org.xwiki.graph;

import java.util.List;

import org.xwiki.stability.Unstable;

@Unstable
public interface Graph<I>
{
    void addEdge(I origin, I relation, Object target) throws GraphException;

    void addEdgeOnce(I origin, I relation, Object target) throws GraphException;

    void addRelation(I identifier, String label, String domain, String image) throws GraphException;

    void addVertex(I identifier, String label) throws GraphException;

    Edge<I> getEdge(I identifier) throws GraphException;

    Edge<I> getEdge(I origin, I relation, I destination) throws GraphException;

    Relation<I> getRelation(I identifier) throws GraphException;

    List<? extends Relation<I>> getRelations() throws GraphException;

    Vertex<I> getVertex(I identifier) throws GraphException;

    void removeEdge(I origin, I relation, Object target) throws GraphException;

    void removeEdges(I origin, I destination) throws GraphException;

    void removeEdgesFrom(I origin) throws GraphException;

    void removeEdgesTo(I destination) throws GraphException;

    // TODO: rename
    void removeEdgesWith(I relation) throws GraphException;

    void removeVertex(I identifier) throws GraphException;

    void updateEdgesTo(I originalDestination, I newDestination) throws GraphException;

    void updateEdgesWith(I originalRelation, I newRelation) throws GraphException;
}
