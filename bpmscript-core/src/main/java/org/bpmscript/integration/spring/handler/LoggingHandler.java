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

import org.springframework.integration.handler.MessageHandler;
import org.springframework.integration.message.Message;

/**
 * Logs messages that are sent to it. If the payload is a {@link Throwable}, logs
 * them as errors.
 */
public class LoggingHandler implements MessageHandler {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    /**
     * Logs messages that are sent to it. If the payload is a {@link Throwable}, logs
     * them as errors.
     */
    public Message<?> handle(Message<?> message) {
        log.info(message);
        Object payload = message.getPayload();
        if (payload instanceof Throwable) {
            Throwable throwable = (Throwable) payload;
            log.error(throwable, throwable);
        }
        return null;
    }

}
