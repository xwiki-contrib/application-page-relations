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

import org.apache.commons.lang3.tuple.Triple;
import org.xwiki.component.annotation.Role;
import org.xwiki.graph.GraphException;
import org.xwiki.graph.GraphFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Role
public interface XWikiGraphFactory extends GraphFactory<DocumentReference>
{
    XWikiVertex createVertex(DocumentReference identifier);

    XWikiEdge createEdge(BaseObject object) throws GraphException;

    XWikiEdge createEdge(DocumentReference origin, DocumentReference relation, Object destination)
            throws GraphException;

    XWikiEdge createEdge(XWikiDocument document, Object destinationOrValue) throws GraphException;

    XWikiRelation createRelation(DocumentReference identifier, String domain, String image, boolean transitive);

    DocumentReference getIdentifier(String relativeName);

    XWikiDocument getDocument(DocumentReference vertex, boolean clone) throws GraphException;

    List<Triple<EntityReference, Class, Class>> getEdgeClasses();

    void saveDocument(XWikiDocument page, String message, String... parameters) throws GraphException;
}
