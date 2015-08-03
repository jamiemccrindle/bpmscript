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

import org.bpmscript.BpmScriptException;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.integration.spring.ReturnAddressSupport;
import org.bpmscript.process.IBpmScriptEngine;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;

/**
 * A spring endpoint that listens for messages and passes them on to
 * the "sendFirst" method of an {@link IBpmScriptEngine}
 */
public class BpmScriptFirstHandler implements MessageHandler {

    private IBpmScriptEngine engine = null;
    private String errorAddress;
    private ISpringMessageSender sender;
    private ReturnAddressSupport returnAddressSupport = new ReturnAddressSupport();

    /**
     * Receives an internal message and interrogates it for the parameters to pass
     * to the {@link IBpmScriptEngine} sendFirst method. Creates a new message
     * to pass into the sendFirst method. The arguments expected in the internal
     * message the same as those on sendFirst
     * 
     * @see IBpmScriptEngine#sendFirst(String, String, String, Object)
     */
    public Message<?> handle(Message<?> message) {
        String operation = (String) message.getHeader().getAttribute("operation");
        String definitionName = (String) message.getHeader().getAttribute("definitionName");
        String parentVersion = (String) message.getHeader().getAttribute("parentVersion");
        message = returnAddressSupport.setSerializeableReturnAddress(message);
        try {
            engine.sendFirst(parentVersion, definitionName, operation, message);
        } catch (BpmScriptException e) {
            ErrorMessage errorMessage = new ErrorMessage(e);
            Object returnAddress = message.getHeader().getReturnAddress();
            if(returnAddress != null) {
                if (returnAddress instanceof MessageChannel) {
                    MessageChannel returnChannel = (MessageChannel) returnAddress;
                    returnChannel.send(errorMessage);
                } else {
                    sender.send((String) returnAddress, errorMessage);
                }
            } else {
                sender.send(errorAddress, errorMessage);
            }
        }
        return null;
    }

    /**
     * @param engine The {@link IBpmScriptEngine} to calls
     */
    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * @param errorAddress where to send exceptions to
     */
    public void setErrorAddress(String errorAddress) {
        this.errorAddress = errorAddress;
    }

    public void setReturnAddressSupport(ReturnAddressSupport returnAddressSupport) {
        this.returnAddressSupport = returnAddressSupport;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

}
