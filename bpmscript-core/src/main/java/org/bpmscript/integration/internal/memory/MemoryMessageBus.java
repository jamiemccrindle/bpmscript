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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bpmscript.integration.IMessageBus;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IMutableAddressRegistry;
import org.bpmscript.integration.internal.NoopMessageSender;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * An in memory message bus
 */
public class MemoryMessageBus implements InitializingBean, DisposableBean, IMessageBus, BeanNameAware {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private BlockingQueue<Object[]> queue = new LinkedBlockingQueue<Object[]>();
    private IMutableAddressRegistry addressRegistry = null;
    private AtomicBoolean running = new AtomicBoolean(false);
    private long timeoutMillis = 100;
    private int threadCount = 10;
    private Executor executor = Executors.newFixedThreadPool(threadCount);
    private IMessageSender auditSender = new NoopMessageSender();
    private IMessageSender messageSender;
    private String name;
    
    /**
     * Send a message to an address
     */
    public void send(String address, IInternalMessage internalMessage) {
        if (log.isDebugEnabled()) {
            log.debug("bus received message " + internalMessage + " for " + address);
        }
        if (address == null) {
            log.warn("address is null for message " + internalMessage);
        }
        if (internalMessage == null) {
            log.warn("message is null for address " + address);
        }

        int indexOfColon = address.indexOf(":");
        if (indexOfColon > 0) {
            String namespace = address.substring(0, indexOfColon);
            if(namespace != name) {
                messageSender.send(address, internalMessage);
            } else {
                auditSender.send(address, internalMessage);
                queue.add(new Object[] { address.substring(indexOfColon + 1), internalMessage });
            }
        } else {
            auditSender.send(name + ":" + address, internalMessage);
            queue.add(new Object[] {address, internalMessage });
        }
    }

    /**
     * Starts up the bus
     */
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < threadCount; i++) {
            executor.execute(new Runnable() {

                public void run() {
                    running.set(true);
                    while (running.get()) {
                        try {
                            Object[] addressAndMessage;
                            addressAndMessage = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                            if (addressAndMessage != null) {
                                String address = (String) addressAndMessage[0];
                                // lookup endpoint and send a message to it...
                                IMessageReceiver receiver = addressRegistry.lookup(address);
                                IInternalMessage internalMessage = (IInternalMessage) addressAndMessage[1];
                                if (receiver == null) {
                                    String errorMessage = "no receiver for address " + address
                                            + " not sending message " + internalMessage;
                                    log.error(errorMessage, new Throwable(errorMessage));
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("bus sending message " + internalMessage + " to " + address);
                                    }
                                    receiver.onMessage(internalMessage);
                                }
                            }
                        } catch (InterruptedException e) {
                            log.warn(e, e);
                        }
                    }
                }

            });
        }
    }

    /**
     * Stops the bus
     */
    public void destroy() throws Exception {
        running.set(false);
    }

    public void setAddressRegistry(IMutableAddressRegistry addressRegistry) {
        this.addressRegistry = addressRegistry;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setAuditSender(IMessageSender auditSender) {
        this.auditSender = auditSender;
    }

    public IMessageReceiver lookup(String address) {
        return addressRegistry.lookup(address);
    }

    public void register(String address, IMessageReceiver receiver) {
        addressRegistry.register(address, receiver);
    }

    public void setMessageSender(IMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        this.name = name;
    }

}
