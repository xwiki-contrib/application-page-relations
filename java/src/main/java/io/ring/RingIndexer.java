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

import org.xwiki.stability.Unstable;

/**
 * RingSet indexer that maintains an index of rings easing RingSet querying.
 *
 * @param <I> vertex identifier class
 */
@Unstable
public interface RingIndexer<I>
{
    /**
     * Adds the given ring to the index.
     *
     * @param ring the ring identifier to index
     * @throws RingException in case an error occurs
     */
    void index(Ring<I> ring) throws RingException;

    /**
     * Removes the given ring from the index.
     *
     * @param ring the ring identifier to be removed
     * @throws RingException in case an error occurs
     */
    void unindex(Ring<I> ring) throws RingException;
}
