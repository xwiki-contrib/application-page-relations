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
package org.xwiki.contrib.graph.internal.model;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

public interface Names
{
    String RELATION_VERTEX_NAME = "Relation";
    String TYPE_VERTEX_NAME = "Type";
    String IS_A_RELATION_NAME = "IsA";
    String IS_CONNECTED_TO_RELATION_NAME = "IsConnectedTo";
    String HAS_IMAGE_RELATION_NAME = "HasImage";
    String HAS_DOMAIN_RELATION_NAME = "HasDomain";
    String IS_TRANSTIVE_RELATION_NAME = "IsTransitive";
    String HAS_RELATION = "has-relation";
    String HAS_DESTINATION = "has-destination";
    String HAS_VALUE = "has-value";
    String GRAPH_NAMESPACE = "Graph";
    EntityReference GRAPH_SPACE_REFERENCE = new EntityReference(GRAPH_NAMESPACE, EntityType.SPACE);
    String GRAPH_CODE_NAMESPACE = GRAPH_NAMESPACE + ".Code";
    EntityReference GRAPH_CODE_SPACE_REFERENCE = new EntityReference("Code", EntityType.SPACE, GRAPH_SPACE_REFERENCE);
}
