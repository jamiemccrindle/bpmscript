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

package org.bpmscript.correlation;

import java.io.Serializable;

/**
 * A provider interface for the correlation service. This should send a matched message
 * to the entity associated with the "replyToken".
 */
public interface ICorrelationChannel {

    /**
     * Send a message to the entity associated with the replyToken. For messaging systems
     * this would often be a combination of "returnAddress" and "correlationId". The
     * condition is that it is serializable, so, for example for JMS queues this may
     * require a lookup step.
     * 
     * @param replyToken the reply token that was registered when the correlation
     *   was added.
     * @param message the message to send (which may be wrapped in another message).
     */
    void send(Serializable replyToken, Object message);
    
    /**
     * Get the content of a message based on the underlying messaging system.
     * 
     * @param message the message to extract the content from
     * @return the content of the message
     */
    Object getContent(Object message);

}
