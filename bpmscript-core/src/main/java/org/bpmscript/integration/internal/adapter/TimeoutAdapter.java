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
package org.bpmscript.integration.internal.adapter;

import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.ResponseMessage;
import org.bpmscript.timeout.ITimeoutListener;
import org.bpmscript.timeout.ITimeoutManager;
import org.bpmscript.timeout.memory.MemoryTimeoutManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Receives timeout messages and passes them to a timeout manager.
 */
public class TimeoutAdapter implements IMessageReceiver, InitializingBean, DisposableBean, ITimeoutListener {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    public static final String TIMEOUT = "timeout";
    private ITimeoutManager timeoutManager = new MemoryTimeoutManager(this);
    private IMessageSender sender;

    public TimeoutAdapter(ITimeoutManager timeoutManager, IMessageSender sender) {
        super();
        this.timeoutManager = timeoutManager;
        this.sender = sender;
    }

    public TimeoutAdapter(IMessageSender sender) {
        super();
        this.sender = sender;
    }

    public TimeoutAdapter() {
        super();
    }

    /**
     * Receives a message, extracts the duration and passes the information
     * to the timeout manager.
     */
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            Long duration = (Long) invocationMessage.getArgs()[0];
            timeoutManager.addTimeout(new Object[] {invocationMessage.getReplyTo(), internalMessage.getCorrelationId(), duration}, duration);
        }
    }
    
    /**
     * Starts the timeout manager
     */
    public void afterPropertiesSet() throws Exception {
        timeoutManager.setTimeoutListener(this);
        timeoutManager.start();
    }

    /**
     * Method that is called on timeout, sends a response message to the process
     * that requested the timeout.
     */
    public void timedOut(Object data) {
        Object[] values = (Object[]) data;
        ResponseMessage message = new ResponseMessage((String) values[1], TIMEOUT);
        if(log.isDebugEnabled()) {
            log.debug("sending timeout for " + message);
        }
        sender.send((String) values[0], message);
    }

    public void setTimeoutManager(ITimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    /**
     * Stops the timeout manager
     */
    public void destroy() throws Exception {
        this.timeoutManager.stop();
    }

}
