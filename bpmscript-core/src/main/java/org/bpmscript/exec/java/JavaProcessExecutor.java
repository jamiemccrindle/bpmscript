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

package org.bpmscript.exec.java;

import org.apache.commons.logging.LogFactory;
import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.IProcessExecutor;
import org.bpmscript.ProcessState;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.exec.CompletedResult;
import org.bpmscript.exec.NextMessage;
import org.bpmscript.exec.PausedResult;
import org.bpmscript.exec.java.seriaize.JavaSerializingContinuationService;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.journal.IContinuationJournal;

/**
 * A Java Process Executor. 
 * 
 * The logic goes something like this:
 * 
 * Send First
 * 
 * Based on the {@link IJavascriptProcessDefinition} that is passed
 * in the Java Process Executor will pick the right class to instantiate and hand to 
 * a JavaChannel.
 * 
 * Once the JavaChannel is finished executing, the JavaChannel is persisted to the
 * {@link JavaSerializingContinuationService} which in turn most likely hands it to
 * a {@link IContinuationJournal}.
 * 
 * Send Next
 * 
 * The right {@link JavaChannel} is looked up using the instance and branch id and
 * the message is passed to it. Once again the result of the execution is stored, 
 * including the {@link JavaChannel} if it is a PAUSED result.
 */
public class JavaProcessExecutor implements IProcessExecutor {

    private JavaSerializingContinuationService continuationService;
    private IScriptChannel channel;

    /**
     * @see IProcessExecutor#kill(String, String, String, String, String, String)
     */
    public IKilledResult kill(String definitionId, String definitionName, String pid, String branch, String version,
            String message) throws BpmScriptException {
        return null;
    }

    /**
     * @see IProcessExecutor#sendFirst(String, String, String, String, IProcessDefinition, Object, String, String)
     */
    public IExecutorResult sendFirst(String definitionId, String pid, String branch, String version,
            IProcessDefinition processDefinition, Object message, String operation, String parentVersion)
            throws BpmScriptException {
        IJavaProcessDefinition javaProcessDefinition = (IJavaProcessDefinition) processDefinition;
        try {
            Class<?> clazz = Class.forName(javaProcessDefinition.getUrl());
            Object newInstance = clazz.newInstance();
            JavaChannel javaChannel = new JavaChannel(channel, newInstance);
            javaChannel.setLog(LogFactory.getLog(processDefinition.getName()));
            ProcessState state = javaChannel.start(parentVersion, pid, branch, version, processDefinition.getName(), operation, message);
            IExecutorResult result = null;
            switch (state) {
            case PAUSED:
                result = new PausedResult(pid, branch, version, javaChannel);
                break;
            case COMPLETED:
                result = new CompletedResult(pid, branch, version, null);
                break;
            }
            continuationService.storeResult(result);
            return result;
        } catch (ClassNotFoundException e) {
            throw new BpmScriptException(e);
        } catch (InstantiationException e) {
            throw new BpmScriptException(e);
        } catch (IllegalAccessException e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * @see IProcessExecutor#sendNext(String, String, String, String, String, String, Object)
     */
    public IExecutorResult sendNext(String definitionId, String definitionName, String pid, String branch,
            String version, String queueId, Object message) throws BpmScriptException {
        JavaChannel javaChannel = continuationService.getContinuation(pid, branch);
        javaChannel.setScriptChannel(channel);
        javaChannel.setLog(LogFactory.getLog(definitionName));
        ProcessState state = javaChannel.receive(new NextMessage(pid, branch, version, queueId, message));
        IExecutorResult result = null;
        switch (state) {
        case PAUSED:
            result = new PausedResult(pid, branch, version, javaChannel);
            break;
        case COMPLETED:
            result = new CompletedResult(pid, branch, version, null);
            break;
        }
        continuationService.storeResult(result);
        return result;
    }

    /**
     * @see IProcessExecutor#validate(IProcessDefinition)
     */
    public String validate(IProcessDefinition processDefinition) throws BpmScriptException {
        return null;
    }

    /**
     * @param continuationService
     */
    public void setContinuationService(JavaSerializingContinuationService continuationService) {
        this.continuationService = continuationService;
    }

    public void setChannel(IScriptChannel channel) {
        this.channel = channel;
    }

}
