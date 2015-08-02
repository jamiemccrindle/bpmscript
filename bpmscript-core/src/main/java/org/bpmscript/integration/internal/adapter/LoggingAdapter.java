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

import org.bpmscript.integration.internal.IExceptionMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;

/**
 * Logs any messages it receives.
 */
public class LoggingAdapter implements IMessageReceiver {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    /**
     * Receive messages and log out their content. If the message contains an error, log the
     * stack trace.
     */
    public void onMessage(IInternalMessage internalMessage) {
        log.info(internalMessage);
        if(internalMessage instanceof IExceptionMessage) {
            IExceptionMessage exceptionMessage = (IExceptionMessage) internalMessage;
            Throwable t = (Throwable) exceptionMessage.getThrowable();
            log.warn(t, t);
        }
    }

}
