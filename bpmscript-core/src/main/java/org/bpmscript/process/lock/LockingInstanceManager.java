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
package org.bpmscript.process.lock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceManager;

/**
 * Locks instances in memory
 */
public class LockingInstanceManager implements IInstanceManager {

	private final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(LockingInstanceManager.class);

	private IInstanceManager instanceManager;

	private static Map<String, AtomicInteger> locks = new ConcurrentHashMap<String, AtomicInteger>();

	public LockingInstanceManager(IInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}
	
	public LockingInstanceManager() {
		
	}

	/**
	 * Locks on the processInstanceId
	 */
	public IExecutorResult doWithInstance(final String processInstanceId,
			final IInstanceCallback callback) throws Exception {
		AtomicInteger lock = null;
		synchronized (locks) {
			lock = locks.get(processInstanceId);
			if (lock == null) {
				lock = new AtomicInteger(1);
				locks.put(processInstanceId, lock);
			} else {
				lock.set(lock.get() + 1);
			}
		}
		IExecutorResult result = null;
		synchronized (lock) {
			try {
				log.debug("locking " + processInstanceId + " " + lock);
				result = instanceManager.doWithInstance(processInstanceId, callback);
			} finally {
				log.debug("unlocking " + processInstanceId);
				lock.set(lock.get() - 1);
				if(lock.get() == 0) {
					locks.remove(processInstanceId);
				}
			}
		}
		return result;
	}

	/**
	 * Delegates
	 */
	public String createInstance(String parentProcessInstanceId, String definitionId, String definitionName, String definitionType, String operation) throws BpmScriptException {
		return instanceManager.createInstance(parentProcessInstanceId, definitionId, definitionName, definitionType, operation);
	}

    /**
     * Delegates
     */
	public List<IInstance> getChildInstances(String processInstanceId) throws BpmScriptException {
		return instanceManager.getChildInstances(processInstanceId);
	}

    /**
     * Delegates
     */
	public IInstance getInstance(String processInstanceId) throws BpmScriptException {
		return instanceManager.getInstance(processInstanceId);
	}

    /**
     * Delegates
     */
	public IPagedResult<IInstance> getInstances(IQuery query) throws BpmScriptException {
		return instanceManager.getInstances(query);
	}

    /**
     * The Instance Manager to delegate to
     */
	public void setInstanceManager(IInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

    /**
     * Delegates
     */
    public IPagedResult<IInstance> getInstancesForDefinition(IQuery query, String definitionId)
            throws BpmScriptException {
        return instanceManager.getInstancesForDefinition(query, definitionId);
    }


}
