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
package io.ring;

/**
 * Factory class used to create Hyperedges, Relations or Vertices.
 *
 * @param <I> vertex identifier
 */
public interface TermFactory<I>
{
    /**
     * Creates a Ring with the given referent, relation and relatum. The relatum can either be a term identifier or a
     * scalar value.
     *
     * @param referent ring referent
     * @param relation ring relation
     * @param relatum ring relatum (identifier or scalar)
     * @return the created Ring
     */
    Ring<I> createRing(I referent, I relation, Object relatum) throws RingException;

    /**
     * Creates a Relation with the given identifier, domain, image and transitivity property.
     *
     * @param identifier relation identifier
     * @param domain relation domain
     * @param image relation image
     * @param transitive relation transitivity value
     * @return the created Relation
     */
    Relation<I> createRelation(I identifier, String domain, String image, boolean transitive) throws RingException;
}
