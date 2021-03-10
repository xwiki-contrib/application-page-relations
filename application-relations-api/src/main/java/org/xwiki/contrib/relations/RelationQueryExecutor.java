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
package org.xwiki.contrib.relations;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.query.QueryException;

import java.util.Arrays;
import java.util.List;

/**
 * Interface (aka Role) of the RelationQueryExecutor.
 *
 * @version $Id$
 */
@Role
public interface RelationQueryExecutor
{

    /**
     * Relation XClass reference.
     */
    EntityReference IS_RELATED_TO_CLASS_REFERENCE =
            new LocalDocumentReference(Arrays.asList("XWiki", "Relations"), "IsRelatedToClass");

    /**
     * Relation XClass main field name.
     */
    String PAGE_FIELD = "page";

    /**
     * Gets incoming relations to a given page from any wiki in the farm.
     * @param reference Reference to page
     * @return List of pages having a relation toward the passed reference
     * @throws QueryException Raised in case of error
     */
    List<DocumentReference> getIncomingRelations(DocumentReference reference) throws QueryException;
}
