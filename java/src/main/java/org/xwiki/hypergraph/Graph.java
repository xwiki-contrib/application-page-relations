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
 * Represents a directed 3-uniform hypergraph where edges are also vertices. Edges involved in this hypergraph class
 * link 3 vertices, where each vertex has a specific role: subject, relation and object. The "I" generics represents an
 * identifier interface used to identify vertices, relations and edges.
 *
 * @param <I> vertex identifier class
 *
 * TODO: - Use iterators instead of collections as returned types
 */
@Unstable
public interface Graph<I>
{

    /**
     * Adds edge between the two given vertex identifiers.
     * @param vertex1 edge vertex one
     * @param vertex2 edge vertex two
     * @throws GraphException in case an error occurs
     */
    void addEdge(I vertex1, I vertex2) throws GraphException;

    /**
     * Adds an edge to the graph from between given subject and the given object using the given relation.
     *
     * @param subject identifier of the subjectvertex
     * @param relation identifier of the edge's relation
     * @param object identifier or value of the object
     * @throws GraphException in case an error occurs
     */
    void addEdge(I subject, I relation, Object object) throws GraphException;

    /**
     * Same as {@link #addEdge(Object, Object, Object)} except the edge is created only if an equivalent one does not
     * exist already.
     *
     * @param subject identifier of the subject vertex
     * @param relation identifier of the edge relation
     * @param object object identifier or value of the edge
     * @throws GraphException in case an error occurs
     */
    void addEdgeOnce(I subject, I relation, Object object) throws GraphException;

    /**
     * Adds a relation to the graph with the given identifier, label, domain and image. See also the definition of a
     * relation's domain and image in graphs at <a href="https://en.wikipedia.org/wiki/Binary_relation">Binary
     * relation</a>.
     *
     * @param identifier the relation identifier
     * @param label the relation label
     * @param domain the relation domain, typically a query describing the set of elements it can be applied to
     * @param image the relations image, typically a query describing the set of elements it acceptss
     * @throws GraphException in case an error occurs
     */
    void addRelation(I identifier, String label, String domain, String image) throws GraphException;

    /**
     * Adds a vertex to this graph.
     * @param identifier vertex identifier
     * @throws GraphException
     */
    void addVertex(I identifier) throws GraphException;

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
     * Returns the edge between the given subject and object with the given relation, if it exists in the graph, null
     * otherwise.
     *
     * @param subject subject vertex identifier
     * @param relation edge relation identifier
     * @param object object vertex identifier
     * @return found edge, if any
     */
    Edge<I> getEdge(I subject, I relation, I object) throws GraphException;

    /**
     * Returns the relation with the given identifier, if it exists in the graph, null otherwise.
     *
     * @param identifier relation identifier
     * @return relation, if found
     */
    Relation<I> getRelation(I identifier) throws GraphException;

    /**
     * Returns all the relations that this graph contains.
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
     * Remove the edge between the two given vertices
     * @param vertex1
     * @param vertex2
     * @throws GraphException
     */
    void removeEdge(I vertex1, I vertex2) throws GraphException;

    /**
     * Removes the edge corresponding to the given edge subject / relation / object, if it exists.
     *
     * @param subject edge subject identifier
     * @param relation edge relation identifier
     * @param object edge object identifier or edge value
     */
    void removeEdge(I subject, I relation, Object object) throws GraphException;

    void removeEdges(I vertex) throws GraphException;

    /**
     * Removes all edges between subject and object, whatever the direction and relation.
     *
     * @param subject edge subject identifier
     * @param object edge object identifier
     */
    void removeEdges(I subject, I object) throws GraphException;

    /**
     * Removes all edges having the given vertex as subject. See also {@link #removeEdgesTo(Object)}
     *
     * @param subject vertex identifier
     */
    void removeEdgesFrom(I subject) throws GraphException;

    /**
     * Removes all edges having the given vertex as object
     *
     * @param object vertex identifier.
     */
    void removeEdgesTo(I object) throws GraphException;

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
     * Updates all edges from / to the given first vertex to the second one.
     * @param vertex1
     * @param vertex2
     * @throws GraphException
     */
    void updateEdges(I vertex1, I vertex2) throws GraphException;

    /**
     * Updates all edges having the given vertex as object to another vertex.
     *
     * @param originalObject original vertex identifier
     * @param otherObject new vertex identifier
     */
    void updateEdgesTo(I originalObject, I otherObject) throws GraphException;

    /**
     * Updates all edges involving the given relation to another relation.
     *
     * @param originalRelation original relation identifier
     * @param otherRelation other relation identifier
     */
    void updateEdgesWith(I originalRelation, I otherRelation) throws GraphException;
}
