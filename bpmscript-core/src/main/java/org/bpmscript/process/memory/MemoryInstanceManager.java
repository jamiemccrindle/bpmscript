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
package org.bpmscript.process.memory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;
import org.bpmscript.paging.IOrderBy;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.lock.LockingInstanceManager;
import org.bpmscript.util.ReflectionComparator;

/**
 * An in memory instance manager. 
 */
public class MemoryInstanceManager implements IInstanceManager {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
	private Map<String, MemoryInstance> memoryInstances = new ConcurrentHashMap<String, MemoryInstance>();

    /**
     * Create a new in memory instance
     */
	public String createInstance(String parentVersion, String definitionId, String definitionName, String definitionType, String operation) throws BpmScriptException {
		String id = UUID.randomUUID().toString();
		MemoryInstance memoryInstance = new MemoryInstance();
		memoryInstance.setId(id);
		memoryInstance.setOperation(operation);
		memoryInstance.setParentVersion(parentVersion);
		memoryInstance.setDefinitionId(definitionId);
        memoryInstance.setDefinitionName(definitionName);
        memoryInstance.setDefinitionType(definitionType);
		memoryInstances.put(id, memoryInstance);
		return id;
	}

	/**
	 * Call the callback with the instance reference by the pid. Should be used in
	 * conjunction with a locking mechanism, e.g. LockingInstanceManager
	 * 
	 * @see LockingInstanceManager
	 */
	public IExecutorResult doWithInstance(String pid, IInstanceCallback callback) throws Exception {
		MemoryInstance processInstance = memoryInstances.get(pid);
        IExecutorResult execute = callback.execute(processInstance);
        if(execute.getProcessState() != ProcessState.IGNORED) {
            processInstance.setLastModified(new Timestamp(System.currentTimeMillis()));
        }
        if(execute.getProcessState() != ProcessState.PAUSED && execute.getProcessState() != ProcessState.IGNORED) {
            log.debug("removing memory instance as it is " + execute.getProcessState().name());
            memoryInstances.remove(pid);
        }
        return execute;
	}

	/**
	 * Get all instances associated with a definition
	 */
    public PagedResult<IInstance> getInstancesForDefinition(IQuery query, String definitionId) throws BpmScriptException {
        List<IInstance> results = new ArrayList<IInstance>();
        for (Map.Entry<String, MemoryInstance> entry : memoryInstances.entrySet()) {
            if(definitionId.equals(entry.getValue().getDefinitionId())) {
                results.add(entry.getValue());
            }
        }
        int totalResults = results.size();
        sortResults(results, query.getOrderBys());
        if(query.getMaxResults() > 0) {
            int maxToGet = query.getFirstResult() + query.getMaxResults() + 1;
            results = results.subList(query.getFirstResult(), results.size() > maxToGet ? maxToGet : results.size());
        }
        return new PagedResult<IInstance>(results, query.getMaxResults(), totalResults);
    }

    /**
     * Get child instances
     */
	public List<IInstance> getChildInstances(String parentVersion) throws BpmScriptException {
		List<IInstance> result = new ArrayList<IInstance>();
		for (Map.Entry<String, MemoryInstance> entry : memoryInstances.entrySet()) {
			if(parentVersion.equals(entry.getValue().getParentVersion())) {
				result.add(entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Get instance for a pid
	 */
	public IInstance getInstance(String pid) throws BpmScriptException {
		return memoryInstances.get(pid);
	}

	/**
	 * Get all instances limited by a query, in a paged form
	 */
	public IPagedResult<IInstance> getInstances(IQuery query) throws BpmScriptException {
		List<IInstance> results = new ArrayList<IInstance>(memoryInstances.values());

		sortResults(results, query.getOrderBys());
		List<IInstance> trimmedResults = null;
		int lastResult = query.getFirstResult() + query.getMaxResults();

		if(query.getFirstResult() >= 0 && query.getMaxResults() >= 0) {
			trimmedResults = new ArrayList<IInstance>(results.subList(query.getFirstResult(), lastResult < results.size() ? lastResult : results.size())); 
		} else {
			trimmedResults = results;
		}

		return new PagedResult<IInstance>(trimmedResults, lastResult > 0 && lastResult < results.size(), results.size());
	}

    /**
     * @param results the results to sort
     * @param orderBys the properties to sort by
     * @throws BpmScriptException if something goes wrong (e.g. one of the properties is not valid)
     */
    private void sortResults(List<IInstance> results, final List<IOrderBy> orderBys) throws BpmScriptException {
        if(orderBys != null && orderBys.size() > 0) {
			try {
				Collections.sort(results, new ReflectionComparator(orderBys));
			} catch (RuntimeException e) {
				Throwable cause = e.getCause();
				if(cause != null && cause instanceof BpmScriptException) {
					throw (BpmScriptException) cause;
				} else {
					throw e;
				}
			}
		}
    }

}
