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

package org.bpmscript.integration.spring;

import org.springframework.integration.bus.MessageBus;
import org.springframework.integration.bus.MessageBusAware;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.message.Message;

/**
 * 
 */
public class SpringMessageSender implements ISpringMessageSender, MessageBusAware {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private MessageBus messageBus;

    /* (non-Javadoc)
     * @see org.bpmscript.integration.spring.ISpringMessageSender#send(java.lang.String, org.springframework.integration.message.Message)
     */
    public void send(String channelName, Message<?> message) {
        MessageChannel channel = messageBus.lookupChannel(channelName);
        if(channel == null) {
            log.error("channel with name " + channelName + " not found");
        }
        channel.send(message);
    }

    /* (non-Javadoc)
     * @see org.springframework.integration.bus.MessageBusAware#setMessageBus(org.springframework.integration.bus.MessageBus)
     */
    public void setMessageBus(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

}
