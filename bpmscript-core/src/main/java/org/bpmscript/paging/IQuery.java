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
package org.bpmscript.paging;

import java.util.List;

/**
 * A paged query, includes a filter, any sorting, the first result and the
 * size of the page.
 */
public interface IQuery {
    
    /**
     * @return a string filter. syntax will depend on the search engine implementation
     *  behind the service that supports paged queries
     */
	String getFilter();
	
	/**
	 * @return get the list of sorting columns, null or empty if there should be any
	 */
	List<IOrderBy> getOrderBys();
	
	/**
	 * @return the start index of the page
	 */
	int getFirstResult();
	
	/**
	 * @return the size of the page or -1 for all results
	 */
	int getMaxResults();
}
