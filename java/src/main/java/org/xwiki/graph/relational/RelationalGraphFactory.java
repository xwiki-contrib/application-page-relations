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
package org.xwiki.graph.relational;

import org.xwiki.graph.GraphException;
import org.xwiki.graph.GraphFactory;
import org.xwiki.graph.Vertex;

/**
 * Factory class used to create edges, relations or vertices.
 * @param <I> vertex identifier
 */
public interface RelationalGraphFactory<I> extends GraphFactory<I>
{
    RelationalEdge<I> createEdge(I origin, I relation, Object target) throws GraphException;

    Relation<I> createRelation(I identifier, String domain, String image, boolean transitive) throws GraphException;

}
