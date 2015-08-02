/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bpmscript.search;

import java.util.Locale;

import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;

/**
 * General purpose search engine
 */
public interface ISearchEngine {
    
    /**
     * Search for search results using a query and specifying a locale
     * for descriptions.
     * 
     * @param query the query used to control what results are returned
     *   as well as how many (i.e. which page)
     * @param locale the language to return the reuslts in
     * @return any results matching the query
     */
    IPagedResult<ISearchResult> search(IQuery query, Locale locale);
}
