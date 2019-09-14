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

import java.util.List;

import org.xwiki.hypergraph.two.Graph;
import org.xwiki.hypergraph.GraphException;
import org.xwiki.stability.Unstable;

/**
 * Represents a directed graph. The I generics represents an identifier interface used to identifiy vertices, relations
 * and edges. The terminology used by the interfaces in this package corresponds to the one used in <a
 * href="https://en.wikipedia.org/wiki/Glossary_of_graph_theory">the mathematical graph theory</a>.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface Hypergraph<I> extends Graph<I>
{
    /**
     * Adds an edge to the graph from the given origin to the given target using the given relation.
     *
     * @param subject identifier of the origin vertex
     * @param relation identifier of the edge's relation
     * @param object identifier or value of the destination
     * @throws GraphException in case an error occurs
     */
    void addEdge(I subject, I relation, Object object) throws GraphException;

    /**
     * Same as {@link #addEdge(Object, Object, Object)} except the edge is created only if an equivalent one does not
     * exist already.
     *
     * @param subject identifier of the origin vertex
     * @param relation identifier of the edge's relation
     * @param object destination identifier or value of the edge's
     * @throws GraphException in case an error occurs
     */
    void addEdgeOnce(I subject, I relation, Object object) throws GraphException;

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
    Hyperedge<I> getEdge(I identifier) throws GraphException;

    /**
     * Returns the edge from origin to destination using relation, if it exists in the graph, null otherwise.
     *
     * @param subject origin vertex
     * @param relation edge relation
     * @param object destination vertex
     * @return found edge, if any
     */
    Hyperedge<I> getEdge(I subject, I relation, I object) throws GraphException;

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
     * Removes the edge corresponding to the given triple origin / relation / destination or value, if it exists.
     *
     * @param subject edge origin identifier
     * @param relation edge relation identifier
     * @param object edge destination identifier or edge value
     */
    void removeEdge(I subject, I relation, Object object) throws GraphException;

    /**
     * Removes all edges between origin and destination and between destination and origin.
     *
     * @param subject edge origin identifier
     * @param object edge destination identifier
     */
    void removeEdges(I subject, I object) throws GraphException;

    /**
     * Removes all edges having the given vertex as origin. See also {@link #removeEdgesTo(Object)}
     *
     * @param subject vertex identifier
     */
    void removeEdgesFrom(I subject) throws GraphException;

    /**
     * Removes all edges having the given vertex as destination.
     */
    void removeEdgesTo(I object) throws GraphException;

    /**
     * Remove all edges involving the given relation.
     *
     * @param relation relation identifier
     */
    void removeEdgesWith(I relation) throws GraphException;

    /**
     * Updates all edges from origin vertex to new origin vertex.
     *
     * @param originalObject original vertex identifier
     * @param newObject new vertex identifier
     */
    void updateEdgesTo(I originalObject, I newObject) throws GraphException;

    /**
     * Updates all edges involving the given relation to a new relation.
     *
     * @param originalRelation original relation identifier
     * @param newRelation new relation identifier
     */
    void updateEdgesWith(I originalRelation, I newRelation) throws GraphException;
}
