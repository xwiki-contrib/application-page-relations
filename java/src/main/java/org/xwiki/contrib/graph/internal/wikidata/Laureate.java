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
package org.xwiki.contrib.graph.internal.wikidata;

public class Laureate extends Entity
{
    protected String typeLabel;

    protected String country;

    protected String countryLabel;

    protected String prize;

    protected String prizeLabel;

    protected String imageUrl;

    public Laureate(String entity, String entityLabel, String entityDescription, String typeLabel, String country,
            String countryLabel, String prize, String prizeLabel, String imageUrl)
    {
        super(entity, entityLabel, entityDescription);
        this.typeLabel = typeLabel;
        this.country = country;
        this.countryLabel = countryLabel;
        this.prize = prize;
        this.prizeLabel = prizeLabel;
        this.imageUrl = imageUrl;
    }

    public String toString()
    {
        return entity + " - " + entityLabel;
    }

    public String getTypeLabel()
    {
        return typeLabel;
    }

    public String getCountry()
    {
        return this.country;
    }

    public String getCountryLabel()
    {
        return countryLabel;
    }

    public String getPrize()
    {
        return prize;
    }

    public String getPrizeLabel()
    {
        return prizeLabel;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }
}
