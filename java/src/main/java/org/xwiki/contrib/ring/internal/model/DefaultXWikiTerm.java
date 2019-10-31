/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.ring.internal.model;

import org.xwiki.contrib.ring.XWikiTerm;
import org.xwiki.model.reference.DocumentReference;

import aek.ring.Term;

/**
 * Default Term implementation. See also: 1) XWikiRestComponent, XWikiResource, ComponentsObjectFactory, 2) ObjectTreeNode, NestedSpacesTree
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
        XWikiTerm term = (XWikiTerm) o;
        if (term.getIdentifier() == null) {
            throw new IllegalArgumentException();
        }
        return term.getIdentifier().equals(this.getIdentifier());
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
