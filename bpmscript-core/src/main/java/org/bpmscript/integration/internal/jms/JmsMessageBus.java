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

package org.bpmscript.integration.internal.jms;

import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.bpmscript.integration.ReplyToOverridingInvocationMessage;
import org.bpmscript.integration.internal.IAddressRegistry;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.NoopMessageSender;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * A version of the internal message bus that uses JMS for sending and receiving 
 * messages.
 */
public class JmsMessageBus implements IMessageSender, MessageListener {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private IAddressRegistry addressRegistry = null;
    private JmsTemplate template = null;
    private ISyncChannelFormatter syncFormatter = null;
    private IMessageSender auditSender = new NoopMessageSender();
    
    private Set<String> syncChannels = new HashSet<String>();

    public JmsMessageBus() {
        super();
    }

    public JmsMessageBus(IAddressRegistry addressRegistry, JmsTemplate template) {
        super();
        this.addressRegistry = addressRegistry;
        this.template = template;
    }

    /**
     * @see org.bpmscript.integration.internal.IMessageSender#send(java.lang.String, org.bpmscript.integration.internal.IInternalMessage)
     */
    public void send(String address, IInternalMessage internalMessage) {
        if(log.isDebugEnabled()) {
            log.debug("bus received message " + internalMessage + " for " + address);
        }
        if(address == null) {
            log.warn("address is null for message " + internalMessage);
        }
        if(internalMessage == null) {
            log.warn("message is null for address " + address);
        }
        auditSender.send(address, internalMessage);
        // this is a message to a sync channel so, the address will be something like: __sync:bpmscript-host-11.1.2.34:sync
        if(syncFormatter.isSync(address)) {
            ILocalAndRemoteAddress localAndRemoteAddress = syncFormatter.parse(address);
            template.convertAndSend(localAndRemoteAddress.getRemoteAddress(), new AddressAndMessage(localAndRemoteAddress.getLocalAddress(), internalMessage));
        }
        if(internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            String replyTo = invocationMessage.getReplyTo();
            if(syncChannels.contains(replyTo)) {
                String syncReplyTo = syncFormatter.format(invocationMessage.getReplyTo());
                template.convertAndSend(new AddressAndMessage(address, new ReplyToOverridingInvocationMessage(invocationMessage, syncReplyTo)));
            } else {
                template.convertAndSend(new AddressAndMessage(address, internalMessage));
            }
        } else {
            template.convertAndSend(new AddressAndMessage(address, internalMessage));
        }
    }

    /* (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message) {
        MessageConverter converter = template.getMessageConverter();
        try {
            AddressAndMessage addressAndMessage = (AddressAndMessage) converter.fromMessage(message);
            String address = addressAndMessage.getAddress();
            IMessageReceiver receiver = addressRegistry.lookup(address);
            if(receiver == null) {
                log.error("could not find receiver for address " + address);
            } else {
                IInternalMessage internalMessage = addressAndMessage.getMessage();
                if(log.isDebugEnabled()) {
                    log.debug("bus sending message " + internalMessage + " to " + address);
                }
                receiver.onMessage(internalMessage);
            }
        } catch (MessageConversionException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAddressRegistry(IAddressRegistry addressRegistry) {
        this.addressRegistry = addressRegistry;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public void setSyncChannels(Set<String> syncChannels) {
        this.syncChannels = syncChannels;
    }

    public void setSyncFormatter(ISyncChannelFormatter syncFormatter) {
        this.syncFormatter = syncFormatter;
    }

    public void setAuditSender(IMessageSender auditSender) {
        this.auditSender = auditSender;
    }
    
}
