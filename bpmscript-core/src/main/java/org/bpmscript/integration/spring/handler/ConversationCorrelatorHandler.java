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
import org.bpmscript.integration.spring.ReturnAddressSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;

/**
 * SpringConversationCorrelator, turns a request message into a response message and 
 * visa versa.
 */
public class ConversationCorrelatorHandler implements MessageHandler {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ISpringMessageSender sender;
    private ReturnAddressSupport returnAddressSupport = new ReturnAddressSupport();
    
    /**
     * Handle a spring integration conversation message
     */
    public Message<?> handle(Message<?> message) {
        Object returnAddress = message.getHeader().getReturnAddress();
        // assume that only invocation messages have return addresses? well, that should work because
        // the response messages shouldn't do...
        if (returnAddress != null) {
            
            Object payload = message.getPayload();
            String address = (String) message.getHeader().getAttribute("conversationReturnAddress");
            String correlationId = (String) message.getHeader().getAttribute("conversationId");
            
            if(payload instanceof Throwable) {
                Throwable t = (Throwable) payload;
                ErrorMessage errorMessage = new ErrorMessage(t);
                errorMessage.getHeader().setCorrelationId(correlationId);
                sender.send(address, errorMessage);
                log.debug("sent error message to " + address);
            } else {
                GenericMessage<Object> wrappedMessage = new GenericMessage<Object>(payload);
                wrappedMessage.getHeader().setReturnAddress(returnAddress);
                wrappedMessage.getHeader().setCorrelationId(message.getHeader().getCorrelationId());
                GenericMessage<Object> messageWrapper = new GenericMessage<Object>(returnAddressSupport.setSerializeableReturnAddress(wrappedMessage));
                messageWrapper.getHeader().setCorrelationId(correlationId);
                sender.send(address, messageWrapper);
                log.debug("sent message to " + address);
            }
        }
        return null;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }
    
    

}
