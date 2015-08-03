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

import com.google.common.collect.ImmutableMap;
import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.integration.spring.ISpringMessageSender;
import org.bpmscript.process.IBpmScriptFacade;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.util.UUID;

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
     */
    public Object call(String definitionName, String methodName, long timeout, Object... args) throws Exception {
        String id = UUID.randomUUID().toString();
        try {
            ImmutableMap<String, Object> headers = ImmutableMap.<String, Object>builder()
                    .put("operation", methodName)
                    .put("definitionName", definitionName)
                    .put(MessageHeaders.REPLY_CHANNEL, returnAddress)
                    .put(IntegrationMessageHeaderAccessor.CORRELATION_ID, id)
                    .build();
            GenericMessage<Object[]> in = new GenericMessage<Object[]>(args, headers);
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
     */
    public void callAsync(String definitionName, String methodName, Object... args) throws Exception {
        String id = UUID.randomUUID().toString();

        ImmutableMap<String, Object> headers = ImmutableMap.<String, Object>builder()
                .put("operation", "sendFirst")
                .put("definitionName", definitionName)
                .put(MessageHeaders.REPLY_CHANNEL, returnAddress)
                .put(IntegrationMessageHeaderAccessor.CORRELATION_ID, id)
                .build();

        GenericMessage<Object[]> in = new GenericMessage<Object[]>(args, headers);

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
