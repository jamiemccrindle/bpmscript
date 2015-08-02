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

import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.IExceptionMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.ResponseMessage;

/**
 * ConversationCorrelator, turns a request message into a response message and 
 * back again. Why you ask? For reasons beyond the ken of mortal men. 
 */
public class ConversationCorrelatorAdapter implements IMessageReceiver {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private IMessageSender sender;
    
    /**
     * Take an invocation message and turn it into either a response or an exception
     * message and send it on to the address that came in on the internalMessage.
     * 
     * The incoming message should be an {@link IInvocationMessage} and should have 3
     * arguments:
     * 
     * <ul>
     *  <li>address</li>
     *  <li>correlationId</li>
     *  <li>object (either a normal object or a {@link Throwable})</li>
     * </ul>
     * 
     * Will pass an {@link ExceptionMessage} back if the object passed in the args is 
     * a {@link Throwable} otherwise will send back a {@link ResponseMessage}. The 
     * contents of the {@link ResponseMessage} will be a callback {@link InvocationMessage}
     * 
     * The {@link ResponseMessage} will have a correlationId that was sent in the args
     * of the {@link IInternalMessage} sent in, while the {@link IInvocationMessage} that
     * is embedded in the {@link ResponseMessage} will be the correlationId of the internalMessage
     * itself.
     * 
     * To explain, we have 3 parties here: The two sides of the conversation and the 
     * conversation correlation adapter. 
     * 
     * <ul>
     * <li>The first party sends an invocation message to the second party.</li>
     * <li>The second party sends an invocation message to the conversation adapter
     *   and the conversation adapter sends the response to the first party</li>
     * <li>Now both sides think they're sending requests and getting responses from 
     *   each other</li>
     * </ul> 
     */
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            Object[] args = invocationMessage.getArgs();
            String address = (String) args[0];
            String correlationId = (String) args[1];
            if(args[2] instanceof Throwable) {
                Throwable t = (Throwable) args[2];
                sender.send(address, new ExceptionMessage(correlationId, t));
            } else {
                sender.send(address, new ResponseMessage(correlationId, new InvocationMessage(internalMessage.getCorrelationId(), invocationMessage.getReplyTo(), args[2])));
            }
        } else if (internalMessage instanceof IResponseMessage) {
            log.warn("can't deal with response message " + internalMessage);
        } else if (internalMessage instanceof IExceptionMessage) {
            log.warn("can't deal with exception message " + internalMessage);
        }
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

}
