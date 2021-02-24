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
package aek.ring;

import org.xwiki.stability.Unstable;

/**
 * Represents a binary relation that is used by rings / edges to connect a term to another one. A Relation is a Term.
 *
 * @param <I> relation and vertex identifier class
 */
@Unstable
public interface Relation<I> extends Term<I>
{
    /**
     * Returns a representation of the relation image: set of vertices this relation can point at. Typically a query
     * that describes a set of vertices. See also {@link #getDomain()}.
     *
     * @return a representation of the relation image
     */
    String getImage();

    /**
     * Returns a representation of the relation domain: set of vertices this relation can apply to. Typically a query
     * that describes a set of vertices. See also {@link #getImage()}.
     *
     * @return a representation of the relation's domain (set of elements this relation can apply to)
     */
    String getDomain();

    /**
     * Returns true if the relation is transitive: if A R B and B R C, then A R C, false otherwise.
     *
     * @return true if this relation is transitive, false otherwise
     */
    boolean isTransitive();

    /**
     * Sets this relation image.
     *
     * @param image relation image
     */
    void setImage(String image);

    /**
     * Sets this relation domain.
     *
     * @param domain relation domain
     */
    void setDomain(String domain);
}
