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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.springframework.integration.bus.MessageBus;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.handler.MessageHandler;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.StringMessage;
import org.springframework.integration.scheduling.Subscription;

/**
 * 
 */
public class SpringIntegrationTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    public void testSpringIntegration() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        MessageBus messageBus = new MessageBus();
        QueueChannel oneChannel = new QueueChannel(10);
        QueueChannel twoChannel = new QueueChannel(10);
        messageBus.registerChannel("onechannel", oneChannel);
        messageBus.registerChannel("twochannel", twoChannel);
        messageBus.registerHandler("onehandler", new MessageHandler(){
            
            public Message<?> handle(Message<?> message) {
                log.info(message.getPayload());
                latch.countDown();
                return new StringMessage("Hi...");
            }
        
        }, new Subscription(oneChannel));
        messageBus.registerHandler("twohandler", new MessageHandler(){
            
            public Message<?> handle(Message<?> message) {
                log.info(message.getPayload());
                latch.countDown();
                return null;
            }
        
        }, new Subscription(twoChannel));
        messageBus.start();
        StringMessage stringMessage = new StringMessage("Hello World!");
        stringMessage.getHeader().setReturnAddress("twochannel");
        oneChannel.send(stringMessage);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        messageBus.stop();
    }
}
