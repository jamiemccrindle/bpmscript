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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the {@link IQuery} interface.
 */
public class Query implements IQuery, Serializable {

	/**
	 * @serial serial version id 
	 */
	private static final long serialVersionUID = -6451150680840278791L;

	/**
	 * A default ALL query that returns all results, starting at the beginning.
	 */
	public static final Query ALL = new Query();
	
	private String filter;
	private List<IOrderBy> orderBys;
	private int firstResult = -1;
	private int maxResults = -1;


    public Query(IOrderBy[] orderBys, int firstResult, int maxResults) {
        this.orderBys = orderBys != null ? Arrays.asList(orderBys) : new ArrayList<IOrderBy>();
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public Query() {
        
    }
    
    public Query(int firstResult, int maxResults) {
        
    }
    
	public int getFirstResult() {
		return firstResult;
	}
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}
	public int getMaxResults() {
		return maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	public List<IOrderBy> getOrderBys() {
		return orderBys;
	}
	public void setOrderBys(List<IOrderBy> orderBys) {
		this.orderBys = orderBys;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public void addOrderBy(String field, boolean asc) {
		if(orderBys == null) {
			orderBys = new ArrayList<IOrderBy>();
		}
		orderBys.add(new OrderBy(field, asc));
	}
	
}
