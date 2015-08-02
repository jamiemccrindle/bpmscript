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
package org.bpmscript.integration.internal.adapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;

/**
 * Records a number of messages (up the maximum) and stores them in a 
 * blocking queue for (potential) later retrieval.
 */
public class RecordingAdapter implements IMessageReceiver {

    private BlockingQueue<IInternalMessage> internalMessages = new LinkedBlockingQueue<IInternalMessage>();
    private int maxMessages = 10000;
    
    /**
     * Stores the message unless the messages stored exceeds the maximum
     */
    public void onMessage(IInternalMessage invocationMessage) {
        if(internalMessages.size() > maxMessages) {
            internalMessages.remove(0);
        }
        internalMessages.add(invocationMessage);
    }

    public BlockingQueue<IInternalMessage> getMessages() {
        return internalMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

}
