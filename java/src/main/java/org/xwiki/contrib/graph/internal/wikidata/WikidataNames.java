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
