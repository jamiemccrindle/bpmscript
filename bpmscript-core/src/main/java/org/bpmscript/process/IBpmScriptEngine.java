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

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;

/**
 * This is the main interface into the BpmScript services. Messages to start up the
 * engine should be passed to the sendFirst method and messages to existing instances
 * should be passed to the sendNext method. Also supports killing running instances
 * and validating process definitions before they're loaded into the system.
 * 
 * The BpmScript methods take a number of parameters that affect which definitions
 * and instances are run.
 * 
 * <h2>Definition</h2>
 * 
 * A Definition represents the definition of a BpmScript process. This is equivalent
 * to the idea of a "class", "workflow" or "business process". A definition is identified
 * by a definitionId. A running definition is known as an instance. 
 * 
 * <h2>Instance</h2>
 * 
 * An Instance is a running "Definition". It is identified by an instanceId or pid. 
 * 
 * <h2>Versions</h2>
 * 
 * An Instance will pause at times and it's state will be written away. Each time this
 * happens a new version will be created.
 * 
 * <h2>Branches</h2>
 * 
 * Occasionally it might be necessary to rollback an instance to a previous version (e.g.
 * because it has failed or because it needs to be retried). When this happens, all
 * new activity will happen on a new "branch" for that instance. All versions for an
 * instance are associated with a branch.
 * 
 * <h2>Queues</h2>
 * 
 * An instance may send out a number of messages in parallel. In order to correlate those
 * messages back to the sender within the instance, a queueId is used.
 * 
 * <h2>Parent Version</h2>
 * 
 * If a BpmScript process calls another BpmScript process, the two can be linked using
 * the "parentVersion" field on sendFirst. This provides the ability to follow the calls
 * from parent instance to child instances down to which version in the parent instance
 * the call originated from (and hence which branch or branches the call was from).
 */
public interface IBpmScriptEngine {

    /**
     * Send the first message to the engine destined for a particular definition.
     * 
     * @param parentVersion the version that the parent instance was on when it made this call
     * @param definitionName the name of the definition to execute
     * @param operation the operation on the definition to execute
     * @param message the message to send to the definition
     * @throws BpmScriptException if there was a problem calling the definition. Exceptions may
     *  come back through here or may come back through an asynchronous message channel.
     */
	void sendFirst(final String parentVersion, String definitionName,
			final String operation, final Object message)
			throws BpmScriptException;

	/**
	 * Send a follow up message to a particular instance.
	 * 
	 * @param pid the instance to send a message to
	 * @param branch which branch to send this message to
	 * @param message the message to send to the instance
	 * @param queueId the queue to target within the instance
	 * @throws BpmScriptException if there is a problem sending the message (e.g. the instance
	 *   doesn't exist)
	 */
    void sendNext(final String pid, final String branch, final Object message,
            final String queueId) throws BpmScriptException;

    /**
     * Kill a particular instance.
     * 
     * @param pid the instance to kill
     * @param branch the branch to kill
     * @param message why the instance was killed
     * @throws BpmScriptException if there was a problem killing the instance.
     */
    void kill(final String pid, final String branch, final String message) throws BpmScriptException;

    /**
     * Validate a process definition. This is useful as a fail fast indication of whether
     * a process passes initial validation. By validating first, fewer corrupt process
     * definitions will get loaded into the engine.
     * 
     * @param processDefinition the process definition to validate
     * @return a unique identifier for the process definition, used when creating it
     * @throws BpmScriptException if the process does not validate or there was a problem
     *   during validation.
     */
    String validate(IProcessDefinition processDefinition) throws BpmScriptException;
}