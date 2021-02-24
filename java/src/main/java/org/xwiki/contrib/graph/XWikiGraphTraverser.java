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
package org.xwiki.contrib.graph;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.hypergraph.GraphException;
import org.xwiki.hypergraph.three.HypergraphTraverser;
import org.xwiki.hypergraph.three.Relation;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.search.solr.internal.metadata.LengthSolrInputDocument;

import com.xpn.xwiki.doc.XWikiDocument;

@Role
public interface XWikiGraphTraverser extends HypergraphTraverser<DocumentReference>
{
    List<Object[]> runEdgeHqlQuery(String propertyName, String destinationId, String wikiId)
            throws QueryException;

    /**
     * Get vertices having an edge toward a given vertex whose relation matches the given one. TODO: rename to
     * getInboundEdges?
     *
     * @param relation a relation
     * @return all vertices pointing at the vertex with the given relation
     */
    List<DocumentReference> getDirectPredecessors(DocumentReference vertex, DocumentReference relation)
            throws GraphException;

    /**
     * @param vertex the origin vertex
     * @return list of vertex references which have the origin vertex as destination for at least one edge
     * @throws GraphException if an error occurs
     */
    List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference vertex) throws GraphException;

    /**
     * @param vertex origin vertex
     * @param relation relation
     * @return list of vertices having the origin vertex as destination for at least one edge with the given relation
     */
    List<DocumentReference> getDirectPredecessorsViaHql(DocumentReference vertex, DocumentReference relation)
            throws GraphException;

    XWikiEdge getFirstEdgeFrom(DocumentReference vertex, DocumentReference relation) throws GraphException;

    List<DocumentReference> getNeighbours(DocumentReference vertex) throws GraphException;

    Query getNeighboursQuery(DocumentReference vertex) throws GraphException;

    List<XWikiEdge> getEdgesFrom(DocumentReference vertex) throws GraphException;

    List<XWikiEdge> getEdgesFrom(DocumentReference vertex, DocumentReference relation) throws GraphException;

    List<XWikiEdge> getEdgesFrom(XWikiDocument page) throws GraphException;

    List<XWikiEdge> getEdges(DocumentReference subject, DocumentReference object) throws GraphException;

    /**
     * @return list of relations that are compatible with the vertex, i.e. whose domains contain the vertex
     */
    List<XWikiRelation> getRelations(DocumentReference vertex, List<? extends Relation<DocumentReference>> relations)
            throws GraphException;

    LengthSolrInputDocument getSolrInputDocument(DocumentReference vertex) throws GraphException;

    List<DocumentReference> run(Query query) throws GraphException;

    List<DocumentReference> search(String solrQuery, String sort, int max) throws GraphException;

    List<DocumentReference> search(String text) throws GraphException;

    List<DocumentReference> search(String text, Relation<DocumentReference> relation) throws GraphException;
}
