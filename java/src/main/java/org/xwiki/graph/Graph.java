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

/**
 * Represents a directed graph. The I generics represents an identifier interface used to identifiy vertices, relations
 * and edges. The terminology used by the interfaces in this package corresponds to the one used in <a
 * href="https://en.wikipedia.org/wiki/Glossary_of_graph_theory">the mathematical graph theory</a>.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface Graph<I>
{
    /**
     * Adds an edge to the graph from the given origin to the given target using the given relation.
     *
     * @param origin identifier of the origin vertex
     * @param relation identifier of the edge's relation
     * @param target identifier or value of the destination
     * @throws GraphException in case an error occurs
     */
    void addEdge(I origin, I relation, Object target) throws GraphException;

    /**
     * Same as {@link #addEdge(Object, Object, Object)} except the edge is created only if an equivalent one does not
     * exist already.
     *
     * @param origin identifier of the origin vertex
     * @param relation identifier of the edge's relation
     * @param target destination identifier or value of the edge's
     * @throws GraphException in case an error occurs
     */
    void addEdgeOnce(I origin, I relation, Object target) throws GraphException;

    /**
     * Adds a relation to the graph with the given identifier, label, domain and image. See also the definition of a
     * relation's domain and image in graphs at <a href="https://en.wikipedia.org/wiki/Binary_relation">Binary
     * relation</a>.
     *
     * @param identifier the relation's identifier
     * @param label the relation's label
     * @param domain the relation's domain, typically a query describing the set of elements it can be applied to
     * @param image the relations's image, typically a query describing the set of elements it acceptss
     * @throws GraphException in case an error occurs
     */
    void addRelation(I identifier, String label, String domain, String image) throws GraphException;

    /**
     * Adds a vertex to the graph with the given identifier and label.
     *
     * @param identifier vertex identifier
     * @param label vertex label
     * @throws GraphException in case an error occurs
     */
    void addVertex(I identifier, String label) throws GraphException;

    /**
     * Returns the edge corresponding to the given identifier
     *
     * @param identifier eddge identifier
     * @return the edge corresponding to the given identifier
     * @throws GraphException in case an error occurs
     */
    Edge<I> getEdge(I identifier) throws GraphException;

    /**
     * Returns the edge from origin to destination using relation, if it exists in the graph, null otherwise.
     *
     * @param origin origin vertex
     * @param relation edge relation
     * @param destination destination vertex
     * @return found edge, if any
     */
    Edge<I> getEdge(I origin, I relation, I destination) throws GraphException;

    /**
     * Returns the relation with the given identifier, if it exists in the graph, null otherwise.
     *
     * @param identifier relation identifier
     * @return relation, if found
     */
    Relation<I> getRelation(I identifier) throws GraphException;

    /**
     * Returns all the relations contained by the graph.
     *
     * @return list of relations
     */
    List<? extends Relation<I>> getRelations() throws GraphException;

    /**
     * Returns the vertex corresponding to the given identifier, if it exists, null otherwise.
     *
     * @param identifier vertex identifier
     */
    Vertex<I> getVertex(I identifier) throws GraphException;

    /**
     * Removes the edge corresponding to the given triple origin / relation / destination or value, if it exists.
     *
     * @param origin edge origin identifier
     * @param relation edge relation identifier
     * @param target edge destination identifier or edge value
     */
    void removeEdge(I origin, I relation, Object target) throws GraphException;

    /**
     * Removes all edges from origin to destination and from destination to origin.
     *
     * @param origin edge origin vertex identifier
     * @param destination edge destination vertex identifier
     */
    void removeEdges(I origin, I destination) throws GraphException;

    /**
     * Removes all edges having the given vertex as origin. See also {@link #removeEdgesTo(Object)}
     *
     * @param origin vertex identifier
     */
    void removeEdgesFrom(I origin) throws GraphException;

    /**
     * Removes all edges having the given vertex as destination.
     */
    void removeEdgesTo(I destination) throws GraphException;

    /**
     * Remove all edges involving the given relation.
     *
     * @param relation relation identifier
     */
    void removeEdgesWith(I relation) throws GraphException;

    /**
     * Removes the vertex with the given identifier, if found.
     *
     * @param identifier vertex identifier
     */
    void removeVertex(I identifier) throws GraphException;

    /**
     * Updates all edges from origin vertex to new origin vertex.
     *
     * @param originalDestination original vertex identifier
     * @param newDestination new vertex identifier
     */
    void updateEdgesTo(I originalDestination, I newDestination) throws GraphException;

    /**
     * Updates all edges involving the given relation to a new relation.
     *
     * @param originalRelation original relation identifier
     * @param newRelation new relation identifier
     */
    void updateEdgesWith(I originalRelation, I newRelation) throws GraphException;
}
