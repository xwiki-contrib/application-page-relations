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

import io.ring.Ring;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.objects.BaseObject;

@Unstable
public interface XWikiRing extends XWikiTerm, Ring<DocumentReference>
{
    BaseObject getBaseObject();

    DocumentReference getReferent();

    DocumentReference getRelation();

    DocumentReference getRelatum();

    Object getValue();

    void setRelation(DocumentReference relation);

    void setRelatum(DocumentReference destination);

    void setValue(Object value);
}
