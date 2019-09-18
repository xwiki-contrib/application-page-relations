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

import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * A an Graph traverser.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface GraphTraverser<I>
{
    /**
     * @return the list of edges from the given subject to the given object
     */
    List<? extends Edge<I>> getEdges(I subject, I object) throws GraphException;

    /**
     * A wildcard is used so that a list of subclass instances can also be returned.
     *
     * @return the list of edges originating from the given vertex
     */
    List<? extends Edge<I>> getEdgesFrom(I vertex) throws GraphException;

    /**
     * @return the list of edges originating from the given vertex with the given relation
     */
    List<? extends Edge<I>> getEdgesFrom(I vertex, I relation) throws GraphException;

    /**
     * @return the first edge originating from the given vertex with the given relation
     */
    Edge<I> getFirstEdgeFrom(I origin, I relation) throws GraphException;

    /**
     * @return the list of relations whose domain is compatible with the given vertex TODO: rename to something
     * mentioning "domain": filterRelationsDomain? searchInRelationsDomain
     */
    List<? extends Relation<I>> getRelations(I Vertex, List<? extends Relation<I>> relations) throws GraphException;

    List<I> search(String text) throws GraphException;

    /**
     * @return the given relation's image set filtered by the input text. Example: if relation is "XWiki.Graph.IsA" and
     * the input is empty, the returned vertices will be all types, because the relation's image is "Type". TODO: rename
     * to filterRelationImage or searchInRelationImage
     */
    List<I> search(String text, Relation<I> relation) throws GraphException;

    List<I> search(String solrQuery, String sort, int max) throws GraphException;
}
