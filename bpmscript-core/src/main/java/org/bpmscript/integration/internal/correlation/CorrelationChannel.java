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

import java.io.Serializable;

import org.bpmscript.correlation.ICorrelationChannel;
import org.bpmscript.integration.internal.IExceptionMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.ResponseMessage;

/**
 * Implementation of the {@link ICorrelationChannel} for the internal messaging
 * system.
 */
public class CorrelationChannel implements ICorrelationChannel {

    private IMessageSender sender;
    
    /**
     * @see org.bpmscript.correlation.ICorrelationChannel#send(java.io.Serializable, java.lang.Object)
     */
    public void send(Serializable replyToken, Object message) {
        IInvocationMessage invocationMessage = (IInvocationMessage) replyToken;
        ResponseMessage responseMessage = new ResponseMessage(invocationMessage.getCorrelationId(), message);
        sender.send(invocationMessage.getReplyTo(), responseMessage);
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    /**
     * Calls either getArgs on an {@link IInvocationMessage}, getContent on {@link IResponseMessage}
     * or getThrowable on {@link IExceptionMessage}
     * 
     * @see org.bpmscript.channel.IScriptChannel#getContent(java.lang.Object)
     */
    public Object getContent(Object message) {
        if (message instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) message;
            return invocationMessage.getArgs();
        } else if (message instanceof IResponseMessage) {
            IResponseMessage responseMessage = (IResponseMessage) message;
            return responseMessage.getContent();
        } else if (message instanceof IExceptionMessage) {
            IExceptionMessage exceptionMessage = (IExceptionMessage) message;
            return exceptionMessage.getThrowable();
        }
        return message;
    }

}
