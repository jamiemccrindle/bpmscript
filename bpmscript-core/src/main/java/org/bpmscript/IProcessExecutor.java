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
package org.bpmscript;

/**
 * Executes definitions. Either it is the start message (i.e. sendFirst) or it's a subsequent message
 * in which case it's sendNext. Kill will attempt to cleanly kill the instance. This gives instances
 * the opportunity to run any kill handlers they have registered.
 */
public interface IProcessExecutor {

    /**
     * @param definitionId the identifier for the definition that is to be run
     * @param pid the instance id for this instance
     * @param branch the starting branch
     * @param version the starting version
     * @param processDefinition the process definition that is run
     * @param message the message to send to the definition
     * @param operation the operation on the definition to execute
     * @param parentVersion the version that the parent instance was on when it made this call
     * @return a result depending on what happened when this process instance was run
     * @throws BpmScriptException if something goes wrong (ideally it should be reported as a 
     *   IFailedResult if the instance throws an exception).
     */
	IExecutorResult sendFirst(String definitionId, final String pid, final String branch, final String version,
			IProcessDefinition processDefinition,
			final Object message, String operation,
			String parentVersion) 
			throws BpmScriptException;

	/**
     * @param definitionId the identifier for the definition that is to be run
	 * @param definitionName
     * @param pid the instance id for this instance
	 * @param branch the branch that this message is for
	 * @param version the version that this message is for
	 * @param queueId the queue that this message is for
	 * @param message the message to deliver
     * @return a result depending on what happened when this process instance was run
	 * @throws BpmScriptException if something goes wrong (ideally it should be reported as a 
	 *   IFailedResult if the instance throws an exception).
	 */
    IExecutorResult sendNext(String definitionId, String definitionName, String pid, String branch, String version, String queueId,
            final Object message) throws BpmScriptException;

    /**
     * Kill an instance and branch. This will only kill one branch, not all of them.
     * 
     * @param definitionId the definition id for the instance
     * @param definitionName the definition name for the instance
     * @param pid the instance id to kill
     * @param branch the branch to kill
     * @param version the version
     * @param message the kill message
     * @return
     * @throws BpmScriptException if something goes wrong (ideally it should be reported as a 
     *   IFailedResult if the instance throws an exception).
     */
    IKilledResult kill(String definitionId, String definitionName, String pid, String branch, String version, String message) 
        throws BpmScriptException;

    /**
     * Validate a process definition
     * @param processDefinition the process definition to validate
     * @return A unique identifier for this process definition. Ideally this identifier should be
     *   the same for the same process definition. 
     * @throws BpmScriptException if the process definition doesn't validate
     */
    String validate(IProcessDefinition processDefinition) throws BpmScriptException;

}