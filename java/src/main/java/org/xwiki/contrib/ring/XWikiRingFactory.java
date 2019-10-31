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
package org.xwiki.contrib.ring;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.xwiki.component.annotation.Role;
import aek.ring.RingException;
import aek.ring.RingFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Role
public interface XWikiRingFactory extends RingFactory<DocumentReference>
{
    XWikiTerm createTerm(DocumentReference identifier);

    XWikiRing createRing(BaseObject object) throws RingException;

    XWikiRing createRing(DocumentReference referent, DocumentReference relation, Object relatum)
            throws RingException;

    XWikiRing createRing(XWikiDocument document, Object destinationOrValue) throws RingException;

    XWikiRelation createRelation(DocumentReference identifier, String domain, String image, boolean transitive);

    DocumentReference getIdentifier(String relativeName);

    XWikiDocument getDocument(DocumentReference vertex, boolean clone) throws RingException;

    List<Triple<EntityReference, Class, Class>> getRingClasses();

    void saveDocument(XWikiDocument page, String message, String... parameters) throws RingException;
}
