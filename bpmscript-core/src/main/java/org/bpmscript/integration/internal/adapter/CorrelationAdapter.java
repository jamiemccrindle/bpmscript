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

import org.bpmscript.correlation.CorrelationException;
import org.bpmscript.correlation.ICorrelation;
import org.bpmscript.correlation.ICorrelationService;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;

/**
 * Listens for messages and passes them to the correlation service.
 * The correlation service needs to be called through the messaging layer so
 * that it can reply later back through the messaging layer using the appropriate
 * return address and correlationId
 */
public class CorrelationAdapter implements IMessageReceiver {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private ICorrelationService correlationService;

    public CorrelationAdapter() {
        super();
    }

    public CorrelationAdapter(ICorrelationService correlationService) {
        super();
        this.correlationService = correlationService;
    }

    /**
     * Unpack the internal message and pass its contents to the addCorrelation
     * method of the correlation service.
     */
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            Object[] args = invocationMessage.getArgs();
            try {
                correlationService.addCorrelation((String) args[0], (String) args[1], (String) args[2], internalMessage, (ICorrelation) args[3], ((Number) args[4]).longValue());
            } catch (CorrelationException e) {
                // TODO: may want to consider sending an exception back at this point...
                log.error(e, e);
            }
        }
    }

    public void setCorrelationService(ICorrelationService correlationService) {
        this.correlationService = correlationService;
    }

}
