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
