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

import org.bpmscript.channel.ISyncChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A sync channel that uses Spring Integration messaging and correlates on the
 * correlation id
 */
public class SpringSyncChannel implements ISyncChannel, MessageHandler {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private Map<String, BlockingQueue<Object>> replies = new ConcurrentHashMap<String, BlockingQueue<Object>>();

    public void expect(String id) {
        replies.put(id, new LinkedBlockingQueue<Object>());
    }

    public void close(String id) {
        replies.remove(id);
    }

    public Object get(String id, long duration) {
        BlockingQueue<Object> blockingQueue = null;
        blockingQueue = replies.get(id);
        if (blockingQueue != null) {
            try {
                if (duration > 0) {
                    return blockingQueue.poll(duration, TimeUnit.MILLISECONDS);
                } else {
                    return blockingQueue.take();
                }
            } catch (InterruptedException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get the correlation id from the message, look up the right blocking queue to add 
     * it to.
     */
    public Message<?> handle(Message<?> message) {
        String id = (String) message.getHeader().getCorrelationId();
        if (id == null) {
            log.error("correlationId for " + message + " is null");
        } else {
            BlockingQueue<Object> blockingQueue = replies.get(id);
            if (blockingQueue != null) {
                blockingQueue.add(message);
            } else {
                log.warn("message with unknown correlation id " + message);
            }
        }
        return null;
    }
}
