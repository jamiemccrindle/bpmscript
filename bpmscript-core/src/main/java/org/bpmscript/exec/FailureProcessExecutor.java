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

package org.bpmscript.exec;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IProcessExecutor;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.channel.IScriptChannel;

/**
 * Handles sending error as reply messages
 */
public class FailureProcessExecutor implements IProcessExecutor {

    private IProcessExecutor delegate;
    private IScriptChannel channel;
    
    /**
     * Reply if the result of an execution was a failure
     * @param pid the instance
     * @param message the message to send
     * @param executorResult the executorResult that is tested
     * @return the executor result that goes in?
     * @throws BpmScriptException if something goes wrong
     */
    protected IExecutorResult replyIfFailure(String pid, Object message, IExecutorResult executorResult) throws BpmScriptException {
        if(executorResult instanceof IFailedResult) {
            channel.reply(pid, message, ((IFailedResult) executorResult).getThrowable());
        }
        return executorResult;
    }
    
    /**
     * Delegates
     */
    public IKilledResult kill(String definitionId, String definitionName, String pid, String branch,
            String version, String message) throws BpmScriptException {
        return delegate.kill(definitionId, definitionName, pid, branch, version, message);
    }

    /**
     * Delegates and replies through the channel if there is a failure
     */
    public IExecutorResult sendFirst(String definitionId, String pid, String branch,
            String version, IProcessDefinition processDefinition, Object message, String operation, String parentVersion)
            throws BpmScriptException {
        return replyIfFailure(pid, message, delegate.sendFirst(definitionId, pid, branch, version, processDefinition, message, operation,
                parentVersion));
    }

    /**
     * Delegates and replies through the channel if there is a failure
     */
    public IExecutorResult sendNext(String definitionId, String definitionName, String pid, String branch,
            String version, String queueId, Object message) throws BpmScriptException {
        return replyIfFailure(pid, message, delegate.sendNext(definitionId, definitionName, pid, branch, version, queueId, message));
    }

    /**
     * Delegates
     */
    public String validate(IProcessDefinition processDefinition) throws BpmScriptException {
        return delegate.validate(processDefinition);
    }

}
