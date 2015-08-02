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

package org.bpmscript.integration.spring.correlation;

import java.util.UUID;

import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.integration.spring.ReturnAddressSupport;
import org.bpmscript.integration.spring.handler.ConversationCorrelatorHandler;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.Message;

/**
 * Spring Conversation Correlator. Sends {@link Message}'s to the 
 * {@link ConversationCorrelatorHandler}
 */
public class SpringConversationCorrelator implements IConversationCorrelator {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ISyncChannel syncChannel;
    private ISpringMessageSender sender;
    private String correlatorAddress;
    private String address;
    private String returnAddress;
    private ReturnAddressSupport returnAddressSupport = new ReturnAddressSupport();

    /**
     * Creates a generic message and embeds the conversation return address, conversationId
     * and payload inside it.
     * 
     * @see org.bpmscript.correlation.IConversationCorrelator#call(java.lang.String, long, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object call(String toCorrelationId, long timeout, Object in) throws Exception {
        String id = UUID.randomUUID().toString();
        try {
            syncChannel.expect(id);
            GenericMessage<Object> invocationMessage = new GenericMessage(in);
            invocationMessage.getHeader().setReturnAddress(returnAddress);
            invocationMessage.getHeader().setCorrelationId(id);
            invocationMessage.getHeader().setAttribute("conversationReturnAddress", address);
            invocationMessage.getHeader().setAttribute("conversationId", toCorrelationId);

            sender.send(correlatorAddress, returnAddressSupport.setSerializeableReturnAddress(invocationMessage));
            // this is all duplicate code... from where?
            Message<Object> result = (Message<Object>) syncChannel.get(id,
                    timeout);
            if (result == null) {
                log.debug("sync service timed out for " + in);
                return null;
            }
            Object payload = result.getPayload();
            if (payload instanceof Throwable) {
                Throwable error = (Throwable) payload;
                if (error instanceof Exception) {
                    throw (Exception) error;
                } else {
                    throw new RuntimeException(error.toString());
                }
            } else {
                return payload;
            }
        } finally {
            syncChannel.close(id);
        }
    }

    public void setSyncChannel(ISyncChannel syncChannel) {
        this.syncChannel = syncChannel;
    }

    public void setCorrelatorAddress(String correlatorAddress) {
        this.correlatorAddress = correlatorAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

}
