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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;

/**
 * An in memory {@link ISyncChannel}. Contains a map of correlationIds to blocking
 * queues. Expect adds an id and queue into the map. Get looks up the appropriate
 * queue and waits for a response to come into the {@link BlockingQueue}. Close
 * removes the correlationId and {@link BlockingQueue} from the map.
 * 
 * Messages are placed in the map when the come in on the onMessage method.
 */
public class MemorySyncChannel implements ISyncChannel, IMessageReceiver {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private Map<String, BlockingQueue<Object>> replies = new ConcurrentHashMap<String, BlockingQueue<Object>>();

    public void expect(String id) {
        replies.put(id, new LinkedBlockingQueue<Object>());
    }

    public void close(String id) {
        replies.remove(id);
    }

    public void onMessage(IInternalMessage internalMessage) {
        String id = internalMessage.getCorrelationId();
        if (id == null) {
            log.error("correlationId for " + internalMessage + " is null");
        } else {
            BlockingQueue<Object> blockingQueue = replies.get(id);
            if (blockingQueue != null) {
                blockingQueue.add(internalMessage);
            }
        }
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

}
