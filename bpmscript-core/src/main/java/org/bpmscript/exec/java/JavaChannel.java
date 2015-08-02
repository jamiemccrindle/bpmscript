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

import static org.bpmscript.ProcessState.FAILED;
import static org.bpmscript.ProcessState.KILLED;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.BeanMap;
import org.apache.commons.logging.Log;
import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.exec.IKillMessage;
import org.bpmscript.exec.INextMessage;
import org.bpmscript.exec.IScriptMessage;
import org.bpmscript.process.IDefinitionConfiguration;

/**
 * The Java Channel contains all of the state for a running instance. It will
 * be serialised at each checkpoint (whenever the process waits for data).
 * 
 * It contains information about the initial request, ongoing information like
 * which branch and version it is on, a map of queues to do correlation of incoming
 * messages to the right handler and the definition object which contains the logic
 * for a particular process.
 */
public class JavaChannel implements Serializable, IJavaChannel {

    /**
     * Serial version id 
     */
    private static final long serialVersionUID = 2575730811264373085L;
    
    private transient IScriptChannel scriptChannel = null;
    private transient Log log;
    private HashMap<String, JavaMessageQueue> queues = new HashMap<String, JavaMessageQueue>();
    private String pid;
    private String currentBranch;
    private String currentVersion;
    private Object definition;
    private Object request;
    private String parentVersion;
    private String operation;
    private String definitionName;

    /**
     * Construct a new Java Channel with the associated scriptChannel (provider)
     * and the definition to run.
     * 
     * @param scriptChannel the underlying script channel
     * @param definition the definition to run.
     */
    public JavaChannel(IScriptChannel scriptChannel, Object definition) {
        this.scriptChannel = scriptChannel;
        this.definition = definition;
    }

    /**
     * Set the script channel after the Java channel is restored
     * @param scriptChannel
     */
    public void setScriptChannel(IScriptChannel scriptChannel) {
        this.scriptChannel = scriptChannel;
    }

    /**
     * Sends out the message as well as a timeout and sets up a queue to receive
     * responses or timeouts.
     * 
     * @see org.bpmscript.exec.java.IJavaChannel#send(java.lang.Object, long, org.bpmscript.exec.java.IJavaMessageHandler)
     */
    public void send(Object message, long timeout, IJavaMessageHandler<?> handler) throws BpmScriptException {
        {
            String queueId = UUID.randomUUID().toString();
            JavaMessageQueue queue = new JavaMessageQueue();
            queue.setHandler(handler);
            queues.put(queueId, queue);
            scriptChannel.send(pid, currentBranch, currentVersion, queueId, message);
        }
        {
            if (timeout > 0) {
                String queueId = UUID.randomUUID().toString();
                JavaMessageQueue queue = new JavaMessageQueue();
                queue.setHandler(handler);
                queues.put(queueId, queue);
                scriptChannel.sendTimeout(pid, currentBranch, currentVersion, queueId, timeout);
            }
        }
    }

    /**
     * @see org.bpmscript.exec.java.IJavaChannel#reply(java.lang.Object)
     */
    public void reply(Object message) throws BpmScriptException {
        scriptChannel.reply(pid, request, message);
    }

    /**
     * Receive a message, find the right queue and pass it to the associated
     * handler
     * 
     * @param scriptMessage the next message that is received
     * @return either PAUSED or COMPLETED
     * @throws BpmScriptException if something goes wrong...
     */
    @SuppressWarnings("unchecked")
    public ProcessState receive(IScriptMessage scriptMessage) throws BpmScriptException {
        wireDefinition();
        if (scriptMessage instanceof INextMessage) {
            INextMessage nextMessage = (INextMessage) scriptMessage;
            this.currentBranch = nextMessage.getBranch();
            this.currentVersion = nextMessage.getVersion();
            String queueId = nextMessage.getQueueId();
            JavaMessageQueue queue = queues.get(queueId);
            if (queue == null) {
                throw new BpmScriptException(queueId);
            }
            IJavaMessageHandler<Object> handler = queue.getHandler();
            return handler.process(nextMessage.getMessage());
        } else if (scriptMessage instanceof IKillMessage) {
            // TODO
            return KILLED;
        }
        return FAILED;
    }

    /**
     * @see IJavaChannel#setLog(Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Start the Java Channel handing the initial request. The start method wires 
     * the definition (does the dependency injection) and then calls the appropriate
     * method on the definition.
     * 
     * @param parentVersion the parent instance version for this process
     * @param pid the parent instance
     * @param branch the branch that this message came in on
     * @param version the starting version
     * @param definitionName the definition name
     * @param operation the operation to run
     * @param request the request message
     * @return
     * @throws BpmScriptException
     */
    @SuppressWarnings("unchecked")
    public ProcessState start(String parentVersion, String pid, String branch, String version, String definitionName, String operation, Object request) throws BpmScriptException {
        this.parentVersion = parentVersion;
        this.pid = pid;
        this.currentBranch = branch;
        this.currentVersion = version;
        this.definitionName = definitionName;
        this.operation = operation;
        this.request = request;
        
        wireDefinition();
        
        try {
            Method method = definition.getClass().getMethod(operation, new Class[] {IJavaChannel.class, Object.class});
            return (ProcessState) method.invoke(definition, this, request);
        } catch (SecurityException e) {
            throw new BpmScriptException(e);
        } catch (NoSuchMethodException e) {
            throw new BpmScriptException(e);
        } catch (IllegalArgumentException e) {
            throw new BpmScriptException(e);
        } catch (IllegalAccessException e) {
            throw new BpmScriptException(e);
        } catch (InvocationTargetException e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * Dependency Injection! Wires using a beanmap...
     */
    @SuppressWarnings("unchecked")
    protected void wireDefinition() {
        BeanMap map = new BeanMap(definition);
        IDefinitionConfiguration definitionConfiguration = scriptChannel.getDefinitionConfiguration(this.definitionName);
        Map<String, Object> properties = definitionConfiguration.getProperties();
        map.putAll(properties);
        if(map.containsKey("log")) {
            map.put("log", log);
        }
    }

    /**
     * @return the queues associated with this channel
     */
    public HashMap<String, JavaMessageQueue> getQueues() {
        return queues;
    }

    /**
     * @return the process instance id
     */
    public String getPid() {
        return pid;
    }

    /**
     * @return the current branch (this will change for each message that comes in)
     */
    public String getCurrentBranch() {
        return currentBranch;
    }

    /**
     * @return the current version (this will change for each message that comes in)
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * @return the definition object that contains the logic for this workflow process
     */
    public Object getDefinition() {
        return definition;
    }

    /**
     * @return the initial request
     */
    public Object getRequest() {
        return request;
    }

    /**
     * @return the parent version of the process instance
     */
    public String getParentVersion() {
        return parentVersion;
    }

    /**
     * @return the operation that was called on the definition
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @see org.bpmscript.exec.java.IJavaChannel#getContent(java.lang.Object)
     */
    public Object getContent(Object message) {
        return scriptChannel.getContent(message);
    }

}
