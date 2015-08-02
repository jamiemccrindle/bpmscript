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

package org.bpmscript.integration.internal.correlation;

import java.util.UUID;

import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.integration.internal.IExceptionMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.InvocationMessage;

/**
 * Allows synchronous calls as part of a conversation
 */
public class ConversationCorrelator implements IConversationCorrelator {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ISyncChannel syncChannel;
    private IMessageSender sender;
    private String correlatorAddress;
    private String address;
    private String replyTo;

    /**
     * @see org.bpmscript.correlation.IConversationCorrelator#call(java.lang.String, long, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object call(String toCorrelationId, long timeout, Object in) throws Exception {
        String id = UUID.randomUUID().toString();
        try {
            syncChannel.expect(id);
            InvocationMessage invocationMessage = new InvocationMessage();
            invocationMessage.setReplyTo(replyTo);
            invocationMessage.setCorrelationId(id);
            invocationMessage.setArgs(address, toCorrelationId, in);
            sender.send(correlatorAddress, invocationMessage);
            // this is all duplicate code...
            IInternalMessage result = (IInternalMessage) syncChannel.get(id,
                    timeout);
            if (result == null) {
                log.debug("sync service timed out for " + in);
                return null;
            }
            if (result instanceof IExceptionMessage) {
                IExceptionMessage exceptionMessage = (IExceptionMessage) result;
                Throwable error = exceptionMessage.getThrowable();
                if (error instanceof Exception) {
                    throw (Exception) error;
                } else {
                    throw new RuntimeException(error.toString());
                }
            } else if (result instanceof IResponseMessage) {
                return ((IResponseMessage)result).getContent();
            } else {
                throw new RuntimeException("message should be a response or exception message");
            }
        } finally {
            syncChannel.close(id);
        }
    }

    public void setSyncChannel(ISyncChannel syncChannel) {
        this.syncChannel = syncChannel;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    public void setCorrelatorAddress(String correlatorAddress) {
        this.correlatorAddress = correlatorAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}
