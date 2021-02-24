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

import java.util.List;

import org.xwiki.stability.Unstable;

/**
 * A RingSet traverser.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface RingTraverser<I>
{
    /**
     * @return the list of edges from the given referent to the given relatum
     */
    List<? extends Ring<I>> getRings(I referent, I relatum) throws RingException;

    /**
     * A wildcard is used so that a list of subclass instances can also be returned.
     *
     * @return the list of edges originating from the given referent
     */
    List<? extends Ring<I>> getRingsFrom(I referent) throws RingException;

    /**
     * @return the list of edges originating from the given referent with the given relation
     */
    List<? extends Ring<I>> getRingsFrom(I referent, I relation) throws RingException;

    /**
     * @return the first edge originating from the given vertex with the given relation
     */
    Ring<I> getFirstRingFrom(I referent, I relation) throws RingException;

    /**
     * @return the list of relations whose domain is compatible with the given vertex TODO: rename to something
     * mentioning "domain": filterRelationsDomain? searchInRelationsDomain
     */
    List<? extends Relation<I>> getRelations(I term, List<? extends Relation<I>> relations) throws RingException;

    List<I> search(String text) throws RingException;

    /**
     * @return the given relation's image set filtered by the input text. Example: if relation is "XWiki.RingSet.IsA" and
     * the input is empty, the returned vertices will be all types, because the relation's image is "Type". TODO: rename
     * to filterRelationImage or searchInRelationImage
     */
    List<I> search(String text, Relation<I> relation) throws RingException;

    List<I> search(String query, String sort, int max) throws RingException;
}
