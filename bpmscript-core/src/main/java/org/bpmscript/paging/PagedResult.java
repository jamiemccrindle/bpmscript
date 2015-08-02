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
import java.util.List;

/**
 * A paged result
 * 
 * @param <T> the type of the page of results
 */
public class PagedResult<T> implements IPagedResult<T>, Serializable {

	private static final long serialVersionUID = -8614709441106769707L;

	private List<T> results;
	private boolean more;
	private int totalResults;
	
	public PagedResult() {
		super();
	}

    public PagedResult(List<T> results, boolean more, int totalResults) {
        this.results = results;
        this.more = more;
        this.totalResults = totalResults;
    }
    
    public PagedResult(List<T> results, int max) {
        if(results.size() < max || max <= 0) {
            this.results = results;
            this.more = false;
        } else {
            this.results = new ArrayList<T>(results.subList(0, max));
            this.more = true;
        }
        this.totalResults = 0;
    }
    
    public PagedResult(List<T> results, int max, int totalResults) {
        if(results.size() < max || max <= 0) {
            this.results = results;
            this.more = false;
        } else {
            this.results = new ArrayList<T>(results.subList(0, max));
            this.more = true;
        }
        this.totalResults = totalResults;
    }
    
	public List<T> getResults() {
		return results;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public boolean isMore() {
		return more;
	}
	
}
