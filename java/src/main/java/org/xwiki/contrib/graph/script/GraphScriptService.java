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
package org.xwiki.contrib.graph.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.graph.XWikiEdge;
import org.xwiki.contrib.graph.XWikiGraph;
import org.xwiki.contrib.graph.XWikiGraphTraverser;
import org.xwiki.contrib.graph.XWikiRelation;
import org.xwiki.contrib.graph.internal.model.Names;
import org.xwiki.hypergraph.GraphException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.script.service.ScriptService;

/**
 * Make the Hypergraph API available to scripting.
 *
 * @version $Id$
 */
@Component
@Named("graph")
@Singleton
public class GraphScriptService implements ScriptService
{
    @Inject
    protected XWikiGraph graph;

    @Inject
    @Named("solr-sql")
    protected XWikiGraphTraverser traverser;

    public void addEdge(DocumentReference origin, DocumentReference relation, DocumentReference destination)
            throws GraphException
    {
        graph.addEdge(origin, relation, destination);
    }

    public List<DocumentReference> getDirectPredecessors(DocumentReference vertex, DocumentReference relation)
            throws GraphException
    {
        return traverser.getDirectPredecessors(vertex, relation);
    }

    public String getDomain(DocumentReference relation) throws GraphException
    {
        return graph.getRelation(relation).getDomain();
    }

    public List<XWikiEdge> getEdgesFrom(DocumentReference identifier) throws GraphException
    {
        return traverser.getEdgesFrom(identifier);
    }

    public List<XWikiEdge> getEdgesFrom(DocumentReference identifier, DocumentReference relation)
            throws GraphException
    {
        return traverser.getEdgesFrom(identifier, relation);
    }

    public String getImage(DocumentReference relation) throws GraphException
    {
        return graph.getRelation(relation).getImage();
    }

    public List<DocumentReference> getNeighbours(DocumentReference vertex) throws GraphException
    {
        return traverser.getNeighbours(vertex);
    }

    public Query getNeighboursQuery(DocumentReference vertex) throws GraphException
    {
        return traverser.getNeighboursQuery(vertex);
    }

    public XWikiRelation getRelation(DocumentReference identifier) throws GraphException
    {
        return graph.getRelation(identifier);
    }

    public List<XWikiRelation> getRelations(DocumentReference vertex) throws GraphException
    {
        return traverser.getRelations(vertex, getRelations());
    }

    public List<XWikiRelation> getRelations() throws GraphException
    {
        return graph.getRelations();
    }

    public void removeEdge(DocumentReference origin, DocumentReference relation, DocumentReference destination)
            throws GraphException
    {
        graph.removeEdge(origin, relation, destination);
    }

    public void removeEdges(DocumentReference origin, DocumentReference destination) throws GraphException
    {
        graph.removeEdges(origin, destination);
    }

    public List<DocumentReference> search(String text) throws GraphException
    {
        return traverser.search(text);
    }

    public List<DocumentReference> search(String text, XWikiRelation relation) throws GraphException
    {
        return traverser.search(text, relation);
    }

    public String getNamespace()
    {
        return Names.GRAPH_CODE_NAMESPACE;
    }

    public String getNamespace(String name)
    {
        if (name.equals("root")) {
            return Names.GRAPH_NAMESPACE;
        }
        return Names.GRAPH_NAMESPACE;
    }
}
