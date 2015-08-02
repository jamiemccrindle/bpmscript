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
package org.bpmscript.process;

import java.util.List;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;

/**
 * Interface for managing running business process instances
 * 
 * @author jamie
 *
 */
public interface IInstanceManager {

    /**
     * Do with a process instance. This is to allow locking of the business
     * process.
     * 
     * @param pid process instance id
     * @param callback what you'd like to do with the process instance
     * @return the result of doing whatever you wanted to do
     * @throws Throwable if something goes wrong
     */
	IExecutorResult doWithInstance(String pid, IInstanceCallback callback) throws Exception;
	
	/**
	 * Create a new process instance based on it's definition and call the operation
	 * specified.
	 * 
	 * @param parentPid the parent process instance. null if there isn't one.
	 * @param definitionId the definition to create an instance for. must not be null
	 * @param operation the operation to call
	 * @return the pid of the new process instance
	 * @throws BpmScriptException if there was a problem creating the instance
	 */
	String createInstance(String parentVersion, String definitionId, String definitionName, String definitionType, String operation) throws BpmScriptException;
	
	/**
	 * Get a process instance from its pid
	 * 
	 * @param pid the pid for the process instance. must not be null.
	 * @return the process instance associated with the pid
	 * @throws BpmScriptException if there was a problem looking up the process instance
	 */
	IInstance getInstance(String pid) throws BpmScriptException;
	
	/**
	 * Get all the child instances associated with this parent version
	 * 
	 * @param parentVersion the version of the parent...
	 * @return the list of child process instances
	 * @throws BpmScriptException if there was a problem looking up the child instances
	 */
	List<IInstance> getChildInstances(String parentVersion) throws BpmScriptException;
	
	/**
	 * Get a paged list of instances based on the incoming query
	 * 
	 * @param query the query used to constrain the results
	 * @return a paged list of instances
	 * @throws BpmScriptException if something goes wrong
	 */
	IPagedResult<IInstance> getInstances(IQuery query) throws BpmScriptException;
	
	/**
	 * Get the instances for a particular definition
	 * 
	 * @param definitionId
	 * @return
	 * @throws BpmScriptException
	 */
	IPagedResult<IInstance> getInstancesForDefinition(IQuery query, String definitionId) throws BpmScriptException;

}