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
