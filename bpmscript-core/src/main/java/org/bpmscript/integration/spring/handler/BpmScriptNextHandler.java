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
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ICorrelationId;
import org.bpmscript.integration.internal.ICorrelationIdFormatter;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.integration.spring.ReturnAddressSupport;
import org.bpmscript.process.IBpmScriptEngine;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.handler.MessageHandler;
import org.springframework.integration.message.ErrorMessage;
import org.springframework.integration.message.Message;

/**
 * Handles sending "next" messages to paused processes
 */
public class BpmScriptNextHandler implements MessageHandler {

    private IBpmScriptEngine engine = null;
    private ICorrelationIdFormatter correlationIdFormatter = CorrelationIdFormatter.DEFAULT_INSTANCE;
    private String errorAddress;
    private ISpringMessageSender sender;
    private ReturnAddressSupport returnAddressSupport = new ReturnAddressSupport();

    /**
     * handles the "next" message, passing it to the {@link IBpmScriptEngine} and sending any errors to the "errorAddress"
     */
    public Message<?> handle(Message<?> message) {
        message = returnAddressSupport.setSerializeableReturnAddress(message);
        try {
            ICorrelationId correlationId = correlationIdFormatter.parse((String) message.getHeader().getCorrelationId());
            engine.sendNext(correlationId.getPid(), correlationId.getBranch(),
                    message, correlationId.getQueueId());
        } catch (BpmScriptException e) {
            Object returnAddress = message.getHeader().getReturnAddress();
            MessageChannel channel = null;
            ErrorMessage errorMessage = new ErrorMessage(e);
            if(returnAddress != null) {
                if (returnAddress instanceof MessageChannel) {
                    channel = (MessageChannel) returnAddress;
                    channel.send(errorMessage);
                } else {
                    sender.send((String) returnAddress, errorMessage);
                }
            } else {
                sender.send(errorAddress, errorMessage);
            }
        }
        return null;
    }

    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

    public void setErrorAddress(String errorAddress) {
        this.errorAddress = errorAddress;
    }

    public void setCorrelationIdFormatter(ICorrelationIdFormatter correlationIdFormatter) {
        this.correlationIdFormatter = correlationIdFormatter;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

    public void setReturnAddressSupport(ReturnAddressSupport returnAddressSupport) {
        this.returnAddressSupport = returnAddressSupport;
    }
}
