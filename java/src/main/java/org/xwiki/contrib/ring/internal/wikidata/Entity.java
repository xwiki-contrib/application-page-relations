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
package org.xwiki.contrib.ring.internal.wikidata;

public class Entity
{
    protected String entity;

    protected String entityLabel;

    protected String entityDescription;

    protected String wikidataId;

    public Entity(String entity, String entityLabel)
    {
        setEntity(entity);
        setLabel(entityLabel);
    }

    public Entity(String entity, String entityLabel, String entityDescription)
    {
        this(entity, entityLabel);
        this.entityDescription = entityDescription;
    }

    public String getEntityLabel()
    {
        return entityLabel;
    }

    public String getEntityDescription()
    {
        return entityDescription;
    }

    public String getWikidataId()
    {
        if (wikidataId == null) {
            int idx = entity.lastIndexOf('/');
            this.wikidataId = entity.substring(idx + 1);
        }
        return wikidataId;
    }

    public void setEntity(String entity)
    {
        if (entity == null) {
            throw new IllegalArgumentException("Attempt to affect null entity to entity" + this);
        }
        this.entity = entity;
    }

    public void setLabel(String label)
    {
        if (label == null) {
            throw new IllegalArgumentException("Attempt to affect null label to entity" + this);
        }
        this.entityLabel = label;
    }
}
