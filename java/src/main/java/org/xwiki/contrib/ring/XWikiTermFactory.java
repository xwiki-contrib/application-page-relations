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
package org.xwiki.contrib.ring;

import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.xwiki.component.annotation.Role;
import aek.ring.RingException;
import aek.ring.TermFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Role
public interface XWikiTermFactory extends TermFactory<DocumentReference>
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
