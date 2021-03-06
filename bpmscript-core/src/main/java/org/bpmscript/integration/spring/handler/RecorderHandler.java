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

package org.bpmscript.integration.spring.handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.integration.handler.MessageHandler;
import org.springframework.integration.message.Message;

/**
 * Records messages sent to it up to maxMessages
 */
public class RecorderHandler implements MessageHandler {
    
    private BlockingQueue<Message<?>> internalMessages = new LinkedBlockingQueue<Message<?>>();
    private int maxMessages = 10000;
    
    public BlockingQueue<Message<?>> getMessages() {
        return internalMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * Receive a message and add to the list of messages if the length of the queue hasn't reached
     * maxMessages. 
     */
    public Message<?> handle(Message<?> message) {
        if(internalMessages.size() > maxMessages) {
            internalMessages.remove(0);
        }
        internalMessages.add(message);
        return null;
    }

}
