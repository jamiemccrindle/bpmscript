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
package org.bpmscript.integration.internal.channel;

import java.util.Map;

import org.bpmscript.BpmScriptException;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.integration.IEnvelope;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.ICorrelationIdFormatter;
import org.bpmscript.integration.internal.IExceptionMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.MapInvocationMessage;
import org.bpmscript.integration.internal.MapResponseMessage;
import org.bpmscript.integration.internal.ResponseMessage;
import org.bpmscript.integration.internal.adapter.TimeoutAdapter;
import org.bpmscript.process.IBpmScriptEngine;
import org.bpmscript.process.IDefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfigurationLookup;
import org.bpmscript.process.spring.ApplicationContextDefinitionConfigurationLookup;

/**
 * Provider implementation for the internal messaging system. Creates InvocationMesssage's 
 * for send and sendOneWay and Response and ErrorMessage's for reply calls.
 */
public class InternalScriptChannel implements IScriptChannel {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private ICorrelationIdFormatter correlationIdFormatter = new CorrelationIdFormatter();
    private IMessageSender sender = null;
    private IDefinitionConfigurationLookup definitionConfigurationLookup = new ApplicationContextDefinitionConfigurationLookup();
    private String replyTo;
    private String timeoutAddress;
    private String errorAddress;

    /**
     * Send a message to an address. The message is either an {@link IEnvelope} which contains an {@link IInternalMessage}
     * or a {@link Map} with an "address" parameter. This makes it easier to support both dynamic Javascript messages
     * and more type safe Java messages.
     * 
     * Send attaches a "replyTo" address and a correlationId to them message so that it returns the the {@link IBpmScriptEngine}
     */
    @SuppressWarnings("unchecked")
    public void send(final String fromPid, final String fromBranch, final String fromVersion, final String queueId,
            Object message) throws BpmScriptException {
        String correlationId = correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId);
        if (message instanceof IEnvelope) {
            IEnvelope envelope = (IEnvelope) message;
            InvocationMessage invocationMessage = ((InvocationMessage) envelope.getMessage());
            invocationMessage.setReplyTo(replyTo);
            invocationMessage.setCorrelationId(correlationId);
            String address = (String) envelope.getAddress();
            sender.send(address, invocationMessage);
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            map.put("correlationId", correlationId);
            map.put("replyTo", replyTo);
            String address = (String) map.get("address");
            MapInvocationMessage result = new MapInvocationMessage(map);
            if (address == null) {
                log.warn("address for message " + result.toString() + " is null");
            }
            sender.send(address, result);
        }
    }

    /**
     * Send a message to an address. The message is either an {@link IEnvelope} which contains an {@link IInternalMessage}
     * or a {@link Map} with an "address" parameter. This makes it easier to support both dynamic Javascript messages
     * and more type safe Java messages.
     */
    @SuppressWarnings("unchecked")
    public void sendOneWay(Object message) throws BpmScriptException {
        if (message instanceof IEnvelope) {
            IEnvelope envelope = (IEnvelope) message;
            sender.send((String) envelope.getAddress(), (IInternalMessage) envelope.getMessage());
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            String address = (String) map.get("address");
            MapInvocationMessage result = new MapInvocationMessage(map);
            if (address == null) {
                log.warn("address for message " + result.toString() + " is null");
            }
            sender.send(address, result);
        }
    }

    /**
     * Sends a timeout message address to the {@link TimeoutAdapter}
     * 
     * Send attaches a "replyTo" address and a correlationId to them message so that it returns the the {@link IBpmScriptEngine}
     */
    public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration)
            throws BpmScriptException {
        InvocationMessage message = new InvocationMessage();
        message.setArgs(duration);
        message.setReplyTo(replyTo);
        message.setCorrelationId(correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId));
        sender.send(timeoutAddress, message);
    }

    /**
     * Reply to a message. The message an {@link IInternalMessage} or a {@link Map} with
     * a "content" key with the return Object.
     */
    @SuppressWarnings("unchecked")
    public void reply(String pid, Object in, Object out) throws BpmScriptException {
        IInternalMessage messageToSend = null;
        String correlationId = ((IInternalMessage) in).getCorrelationId();
        if (out instanceof ExceptionMessage) {
            ExceptionMessage exceptionMessage = (ExceptionMessage) out;
            exceptionMessage.setCorrelationId(correlationId);
            messageToSend = exceptionMessage;
        } else if (out instanceof ResponseMessage) {
            ResponseMessage responseMessage = (ResponseMessage) out;
            responseMessage.setCorrelationId(correlationId);
            messageToSend = responseMessage;
        } else {
            Map<String, Object> map = (Map<String, Object>) out;
            Object content = map.get("content");
            if(content != null && content instanceof Throwable) {
                messageToSend = new ExceptionMessage(correlationId, (Throwable) content);
            } else {
                messageToSend = new MapResponseMessage(map);
            }
            map.put("correlationId", correlationId);
        }
        if (in instanceof IInvocationMessage) {
            final IInvocationMessage inMessage = (IInvocationMessage) in;
            sender.send(inMessage.getReplyTo(), messageToSend);
        } else {
            sender.send(errorAddress, messageToSend);
        }
    }

    public void setCorrelationIdFormatter(ICorrelationIdFormatter correlationIdFormatter) {
        this.correlationIdFormatter = correlationIdFormatter;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public void setTimeoutAddress(String timeoutAddress) {
        this.timeoutAddress = timeoutAddress;
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

    public void setErrorAddress(String errorAddress) {
        this.errorAddress = errorAddress;
    }

    /**
     * @see org.bpmscript.channel.IScriptChannel#getDefinitionConfiguration(java.lang.String)
     */
    public IDefinitionConfiguration getDefinitionConfiguration(String definitionName) {
        return definitionConfigurationLookup.getDefinitionConfiguration(definitionName);
    }

    public void setDefinitionConfigurationLookup(IDefinitionConfigurationLookup definitionConfigurationLookup) {
        this.definitionConfigurationLookup = definitionConfigurationLookup;
    }

    /**
     * @see org.bpmscript.channel.ISendChannel#createCallback(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object createCallback(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        String correlationId = correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId);
        if (message instanceof InvocationMessage) {
            InvocationMessage invocationMessage = ((InvocationMessage) message);
            invocationMessage.setReplyTo(replyTo);
            invocationMessage.setCorrelationId(correlationId);
            return invocationMessage;
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            map.put("correlationId", correlationId);
            map.put("replyTo", replyTo);
            MapInvocationMessage result = new MapInvocationMessage(map);
            return result;
        }
    }

}
