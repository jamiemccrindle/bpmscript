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
package org.bpmscript.integration.spring.channel;

import org.bpmscript.BpmScriptException;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.integration.IEnvelope;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ICorrelationIdFormatter;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.integration.spring.ISpringScriptMessageNames;
import org.bpmscript.integration.spring.ReturnAddressSupport;
import org.bpmscript.integration.spring.SpringScriptMessageNames;
import org.bpmscript.process.IDefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfigurationLookup;
import org.bpmscript.process.spring.ApplicationContextDefinitionConfigurationLookup;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.GenericMessage;

import java.util.Date;
import java.util.Map;

/**
 * Spring Integration implementation of the {@link IScriptChannel} interface. Sends
 * Spring Integration Message objects to the message bus.
 */
public class SpringScriptChannel implements IScriptChannel {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private ICorrelationIdFormatter correlationIdFormatter = new CorrelationIdFormatter();
    private IDefinitionConfigurationLookup definitionConfigurationLookup = new ApplicationContextDefinitionConfigurationLookup();
    private String returnAddress;
    private String timeoutAddress;
    private String errorAddress;
    private ISpringMessageSender sender;
    private ReturnAddressSupport returnAddressSupport = new ReturnAddressSupport();
    private ISpringScriptMessageNames springScriptMessageNames = SpringScriptMessageNames.DEFAULT_INSTANCE;

    @SuppressWarnings("unchecked")
    public void send(final String fromPid, final String fromBranch, final String fromVersion, final String queueId,
            Object message) throws BpmScriptException {
        String correlationId = correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId);
        if (message instanceof IEnvelope) {
            IEnvelope envelope = (IEnvelope) message;
            Message<?> result = ((Message<?>) envelope.getMessage());
            result.getHeader().setCorrelationId(correlationId);
            result.getHeader().setReturnAddress(returnAddress);
            sender.send((String) envelope.getAddress(), result);
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            GenericMessage<Object> result = new GenericMessage<Object>((Object) map.get(springScriptMessageNames
                    .getRequestContentName()));
            result.getHeader().setCorrelationId(correlationId);
            result.getHeader().setReturnAddress(returnAddress);
            setHeaders(map, result);
            String address = (String) map.get(springScriptMessageNames.getAddressName());
            if (address == null) {
                log.warn("address for message " + result.toString() + " is null");
            }
            sender.send(address, result);
        }
    }

    /**
     * @param map
     * @param result
     */
    @SuppressWarnings("unchecked")
    protected void setHeaders(Map<String, Object> map, Message<?> result) {
        Object attributesObject = map.get(springScriptMessageNames.getAttributesName());
        Object propertiesObject = map.get(springScriptMessageNames.getPropertiesName());
        if (attributesObject != null && attributesObject instanceof Map) {
            Map<String, Object> attributes = (Map<String, Object>) attributesObject;
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key != null && value != null) {
                    result.getHeader().setAttribute(key, value);
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("key or value null for " + key + " " + value + " " + map);
                    }
                }
            }
        }
        if (propertiesObject != null && propertiesObject instanceof Map) {
            Map<String, String> properties = (Map<String, String>) propertiesObject;
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key != null && value != null) {
                    result.getHeader().setAttribute(key, value);
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("key or value null for " + key + " " + value + " " + map);
                    }
                }
            }
        }
        Date expiration = (Date) map.get(springScriptMessageNames.getExpirationName());
        String priority = (String) map.get(springScriptMessageNames.getPriorityName());
        Integer sequenceNumber = (Integer) map.get(springScriptMessageNames.getSequenceNumberName());
        Integer sequenceSize = (Integer) map.get(springScriptMessageNames.getSequenceSizeName());
        
        if(expiration != null) result.getHeader().setExpiration(expiration);
        if(priority != null) result.getHeader().setPriority(MessagePriority.valueOf(priority));
        if(sequenceNumber != null) result.getHeader().setSequenceNumber(sequenceNumber);
        if(sequenceSize != null) result.getHeader().setSequenceSize(sequenceSize);
    }

    @SuppressWarnings("unchecked")
    public void sendOneWay(Object message) throws BpmScriptException {
        if (message instanceof IEnvelope) {
            IEnvelope envelope = (IEnvelope) message;
            Message<?> result = ((Message<?>) envelope.getMessage());
            sender.send((String) envelope.getAddress(), result);
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            GenericMessage<Object> result = new GenericMessage<Object>((Object) map.get(springScriptMessageNames
                    .getRequestContentName()));
            String address = (String) map.get(springScriptMessageNames.getAddressName());
            setHeaders(map, result);
            if (address == null) {
                log.warn("address for message " + result.toString() + " is null");
            }
            sender.send(address, result);
        }
    }

    public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration)
            throws BpmScriptException {
        GenericMessage<Long> result = new GenericMessage<Long>(duration);
        result.getHeader().setReturnAddress(returnAddress);
        result.getHeader().setCorrelationId(correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId));
        sender.send(timeoutAddress, result);
    }

    @SuppressWarnings("unchecked")
    public void reply(String pid, Object in, Object out) throws BpmScriptException {
        Message<?> inMessage = (Message<?>) in;
        Message<?> messageToSend = null;

        if (out instanceof ErrorMessage) {
            Throwable throwable = (Throwable) out;
            messageToSend = new ErrorMessage(throwable);
        } else {
            Map<String, Object> map = (Map<String, Object>) out;
            messageToSend = new GenericMessage(map.get(springScriptMessageNames.getResponseContentName()));
            setHeaders(map, messageToSend);
        }
        Object correlationId = (Object) inMessage.getHeader().getCorrelationId();
        messageToSend.getHeader().setCorrelationId(correlationId);

        Object replyAddress = returnAddressSupport.getSerializeableReturnAddress(inMessage);

        if (replyAddress != null) {
            if (replyAddress instanceof MessageChannel) {
                MessageChannel channel = (MessageChannel) replyAddress;
                channel.send(messageToSend);
            } else {
                sender.send((String) replyAddress, messageToSend);
            }
        } else {
            log.warn("replyAddress is null, so couldn't send " + messageToSend + " sending to " + errorAddress
                    + " instead");
            sender.send(errorAddress, messageToSend);
        }
    }

    public void setCorrelationIdFormatter(ICorrelationIdFormatter correlationIdFormatter) {
        this.correlationIdFormatter = correlationIdFormatter;
    }

    public void setTimeoutAddress(String timeoutAddress) {
        this.timeoutAddress = timeoutAddress;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bpmscript.channel.IScriptChannel#getContent(java.lang.Object)
     */
    public Object getContent(Object messageObject) {
        if (messageObject == null) {
            log.debug("message is null");
            return null;
        }
        Message<?> message = (Message<?>) messageObject;
        return message.getPayload();
    }

    public void setErrorAddress(String errorAddress) {
        this.errorAddress = errorAddress;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    public void setReturnAddressSupport(ReturnAddressSupport returnAddressSupport) {
        this.returnAddressSupport = returnAddressSupport;
    }

    /**
     * @see org.bpmscript.channel.IScriptChannel#getDefinitionConfiguration(java.lang.String)
     */
    public IDefinitionConfiguration getDefinitionConfiguration(String definitionName) {
        return definitionConfigurationLookup.getDefinitionConfiguration(definitionName);
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

    public void setDefinitionConfigurationLookup(IDefinitionConfigurationLookup definitionConfigurationLookup) {
        this.definitionConfigurationLookup = definitionConfigurationLookup;
    }

    public void setSpringScriptMessageNames(ISpringScriptMessageNames springScriptMessageNames) {
        this.springScriptMessageNames = springScriptMessageNames;
    }

    /**
     * @see org.bpmscript.channel.ISendChannel#createCallback(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object createCallback(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        String correlationId = correlationIdFormatter.format(fromPid, fromBranch, fromVersion, queueId);
        if (message instanceof Message<?>) {
            Message<?> result = ((Message<?>) message);
            result.getHeader().setCorrelationId(correlationId);
            result.getHeader().setReturnAddress(returnAddress);
            return result;
        } else {
            Map<String, Object> map = (Map<String, Object>) message;
            GenericMessage<Object> result = new GenericMessage<Object>((Object) map.get(springScriptMessageNames
                    .getRequestContentName()));
            result.getHeader().setCorrelationId(correlationId);
            result.getHeader().setReturnAddress(returnAddress);
            setHeaders(map, result);
            return result;
        }
    }

}
