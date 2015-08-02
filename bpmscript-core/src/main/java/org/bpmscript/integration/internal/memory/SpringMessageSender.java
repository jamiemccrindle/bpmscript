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

package org.bpmscript.integration.internal.memory;

import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Sends messages to a message bus looked up by bean name
 */
public class SpringMessageSender implements IMessageSender, ApplicationContextAware {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ApplicationContext applicationContext;

    /**
     * @see org.bpmscript.integration.internal.IMessageSender#send(java.lang.String, org.bpmscript.integration.internal.IInternalMessage)
     */
    public void send(String address, IInternalMessage internalMessage) {
        int indexOfColon = address.indexOf(":");
        if(indexOfColon > 0) {
            String beanName = address.substring(0, indexOfColon);
            IMessageSender bus = (IMessageSender) applicationContext.getBean(beanName);
            String localaddress = address.substring(indexOfColon + 1);
            if(internalMessage instanceof IInvocationMessage) {
                IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
                bus.send(localaddress, invocationMessage);
            } else {
                bus.send(localaddress, internalMessage);
            }
        } else {
            log.warn("could not send message " + internalMessage + " to address " + address);
        }
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
