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

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public interface WikidataNames
{
    String PERSON_TYPE_NAME = "person";
    String COUNTRY_TYPE_NAME = "country";
    String AWARD_TYPE_NAME = "award";
    String ORGANIZATION_TYPE_NAME = "organization";
    String HAS_COUNTRY = "has-country";
    String HAS_AWARD = "has-award";
    String HAS_WIKIDATA_ID = "has-wikidata-identifier";
    Type LAUREATE = new TypeToken<List<Laureate>>()
    {
    }.getType();
}
