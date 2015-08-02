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

package org.bpmscript.integration.internal;

import org.bpmscript.integration.internal.adapter.ReflectionAdapter;
import org.bpmscript.integration.internal.memory.MemoryAddressRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Treats an application context as an address registry with all the beans registered as {@link IMessageReceiver}'s
 * with their bean names as their addresses.
 */
public class SpringAddressRegistry implements IMutableAddressRegistry, ApplicationContextAware {

    private IMutableAddressRegistry mutableAddressRegistry = new MemoryAddressRegistry();
    private ApplicationContext context = null;
    private String senderBean = null;
    private IMessageSender sender;
    
    public SpringAddressRegistry() {
        super();
    }

    public SpringAddressRegistry(IMessageSender sender, ApplicationContext context) {
        super();
        this.sender = sender;
        this.context = context;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    /**
     * @see org.bpmscript.integration.memory.IAddressRegistry#lookup(java.lang.String)
     */
    public IMessageReceiver lookup(String address) {
        Object bean = mutableAddressRegistry.lookup(address);
        if(bean == null) {
            bean = context.getBean(address);
        }
        IMessageSender messageSender = sender;
        if(messageSender == null) {
            messageSender = (IMessageSender) context.getBean(this.senderBean);
        }
        if(bean instanceof IMessageReceiver) {
            return (IMessageReceiver) bean;
        } else {
            return new ReflectionAdapter(messageSender, bean);
        }
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void setSenderBean(String senderBean) {
        this.senderBean = senderBean;
    }

    /**
     * @see org.bpmscript.integration.internal.IMutableAddressRegistry#register(java.lang.String, org.bpmscript.integration.internal.IMessageReceiver)
     */
    public void register(String address, IMessageReceiver receiver) {
        mutableAddressRegistry.register(address, receiver);
    }

    public void setMutableAddressRegistry(IMutableAddressRegistry mutableAddressRegistry) {
        this.mutableAddressRegistry = mutableAddressRegistry;
    }
    
}
