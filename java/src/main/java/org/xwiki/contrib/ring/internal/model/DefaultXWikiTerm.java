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
package org.xwiki.contrib.ring.internal.model;

import org.xwiki.contrib.ring.XWikiTerm;
import aek.ring.Term;
import org.xwiki.model.reference.DocumentReference;

/**
 * Default Term implementation. See also: 1) XWikiRestComponent, XWikiResource, ComponentsObjectFactory, 2)
 * ObjectTreeNode, NestedSpacesTree
 *
 * @version $Id$
 */
public class DefaultXWikiTerm implements XWikiTerm
{
    protected DocumentReference identifier;

    public DefaultXWikiTerm(DocumentReference reference)
    {
        this.identifier = reference;
    }

    public Term<DocumentReference> clone()
    {
        XWikiTerm clone = new DefaultXWikiTerm(identifier);
        return clone;
    }

    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof XWikiTerm)) {
            return false;
        }
        XWikiTerm vertex = (XWikiTerm) o;
        if (vertex.getIdentifier() == null) {
            throw new IllegalArgumentException();
        }
        return vertex.getIdentifier().equals(this.getIdentifier());
    }


    public DocumentReference getIdentifier()
    {
        return identifier;
    }

    public int hashCode()
    {
        return getIdentifier().hashCode();
    }

    /**
     * Returns a string representation of this document.
     *
     * @return the string representation
     */
    public String toString()
    {
        return getIdentifier().toString();
    }
}
