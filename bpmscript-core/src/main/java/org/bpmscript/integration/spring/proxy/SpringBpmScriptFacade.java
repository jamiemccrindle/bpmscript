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

package org.bpmscript.integration.spring.proxy;

import java.util.UUID;

import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.process.IBpmScriptFacade;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.Message;

/**
 * Synchronous / Asynchronous way of calling scripts. Useful for creating proxies that
 * implement particular interfaces...
 */
public class SpringBpmScriptFacade implements IBpmScriptFacade {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ISyncChannel syncChannel;
    private ISpringMessageSender sender;
    private String returnAddress;
    private String address;

    public SpringBpmScriptFacade(ISyncChannel syncChannel, ISpringMessageSender sender, String returnAddress, String address) {
        super();
        this.syncChannel = syncChannel;
        this.sender = sender;
        this.returnAddress = returnAddress;
        this.address = address;
    }

    public SpringBpmScriptFacade() {
        super();
    }

    /**
     * Call a definition using it's name and methodName and send it an array of args. 
     * Wait for timeout milliseconds for the response.
     * 
     * @see org.bpmscript.integration.internal.proxy.IBpmScriptFacade#call(java.lang.String, java.lang.String, java.lang.Object[])
     */
    public Object call(String definitionName, String methodName, long timeout, Object... args) throws Exception {
        String id = UUID.randomUUID().toString();
        try {
            GenericMessage<Object[]> in = new GenericMessage<Object[]>(args);
            in.getHeader().setAttribute("operation", methodName);
            in.getHeader().setAttribute("definitionName", definitionName);
            in.getHeader().setReturnAddress(returnAddress);
            in.getHeader().setCorrelationId(id);
            syncChannel.expect(id);
            sender.send(address, in);

            Message<?> result = (Message<?>) syncChannel.get(id,
                    timeout);
            if (result == null) {
                log.debug("sync service timed out for " + in);
                return null;
            }
            // TODO: this is where the code is copied... see spring conversation correlator
            Object payload = result.getPayload();
            if (payload instanceof Throwable) {
                Throwable error = (Throwable) payload;
                if (error instanceof Exception) {
                    throw (Exception) error;
                } else {
                    throw new RuntimeException(error.toString());
                }
            } else {
                return payload;
            }
        } finally {
            syncChannel.close(id);
        }
    }

    /**
     * Send a one way message to a definition
     * @see org.bpmscript.integration.internal.proxy.IBpmScriptFacade#callAsync(java.lang.String, java.lang.String, java.lang.Object[])
     */
    public void callAsync(String definitionName, String methodName, Object... args) throws Exception {
        String id = UUID.randomUUID().toString();
        GenericMessage<Object[]> in = new GenericMessage<Object[]>(args);
        in.getHeader().setAttribute("operation", "sendFirst");
        in.getHeader().setAttribute("definitionName", definitionName);
        in.getHeader().setReturnAddress(returnAddress);
        in.getHeader().setCorrelationId(id);
        sender.send(address, in);
    }

    public void setSyncChannel(ISyncChannel syncChannel) {
        this.syncChannel = syncChannel;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSender(ISpringMessageSender sender) {
        this.sender = sender;
    }

    public void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }
    
}
