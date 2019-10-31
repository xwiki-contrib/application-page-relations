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

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

public interface Names
{
    String RELATION_TERM_NAME = "Relation";
    String TYPE_TERM_NAME = "Type";
    String IS_A_RELATION_NAME = "IsA";
    String IS_CONNECTED_TO_RELATION_NAME = "IsConnectedTo";
    String HAS_IMAGE_RELATION_NAME = "HasImage";
    String HAS_DOMAIN_RELATION_NAME = "HasDomain";
    String IS_TRANSTIVE_RELATION_NAME = "IsTransitive";
    String HAS_RELATION = "has-relation";
    String HAS_RELATUM = "has-relatum";
    String HAS_VALUE = "has-value";
    String HAS_PROPERTIES = "properties";
    String RING_NEXUS_NAMESPACE = "Ring";
    EntityReference RING_NEXUS_SPACE_REFERENCE = new EntityReference(RING_NEXUS_NAMESPACE, EntityType.SPACE);
    String RING_NEXUS_CODE_NAMESPACE = RING_NEXUS_NAMESPACE + ".Code";
    EntityReference RING_CODE_SPACE_REFERENCE = new EntityReference("Code", EntityType.SPACE,
            RING_NEXUS_SPACE_REFERENCE);
}
