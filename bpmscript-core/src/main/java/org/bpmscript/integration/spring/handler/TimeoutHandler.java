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

package org.bpmscript.integration.spring.handler;

import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.timeout.ITimeoutListener;
import org.bpmscript.timeout.ITimeoutManager;
import org.bpmscript.timeout.memory.MemoryTimeoutManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.handler.MessageHandler;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.StringMessage;

/**
 * Receives timeout messages and passes them to a timeout manager.
 */
public class TimeoutHandler implements MessageHandler, InitializingBean, DisposableBean, ITimeoutListener {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public static final String TIMEOUT = "timeout";
    private ITimeoutManager timeoutManager = new MemoryTimeoutManager(this);
    private ISpringMessageSender sender;

    /**
     * Receives a message, extracts the duration and passes the information
     * to the timeout manager.
     */
    public Message<?> handle(Message<?> message) {
        Object returnAddressObject = message.getHeader().getReturnAddress();
        if (returnAddressObject != null) {
            String returnAddress = null;
            if (returnAddressObject instanceof MessageChannel) {
                MessageChannel returnAddressChannel = (MessageChannel) returnAddressObject;
                returnAddress = returnAddressChannel.getName();
            } else {
                returnAddress = returnAddressObject.toString();
            }
            Long duration = (Long) message.getPayload();
            timeoutManager.addTimeout(new Object[] {returnAddress, message.getHeader().getCorrelationId(), duration}, duration);
        } else {
            log.warn("return address not set for message " + message);
        }
        return null;
    }

    /**
     * Starts the timeout manager
     */
    public void afterPropertiesSet() throws Exception {
        timeoutManager.setTimeoutListener(this);
        timeoutManager.start();
    }

    /**
     * Stops the timeout manager
     */
    public void destroy() throws Exception {
        this.timeoutManager.stop();
    }

    /**
     * Method that is called on timeout, sends a response message to the process
     * that requested the timeout.
     */
    public void timedOut(Object data) {
        Object[] values = (Object[]) data;
        StringMessage message = new StringMessage(TIMEOUT);
        message.getHeader().setCorrelationId((String) values[1]);
        if(log.isDebugEnabled()) {
            log.debug("sending timeout for " + message);
        }
        sender.send((String) values[0], message);
    }

    public void setTimeoutManager(ITimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

}
