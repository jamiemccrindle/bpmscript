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

import org.bpmscript.BpmScriptException;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.ICorrelationId;
import org.bpmscript.integration.internal.ICorrelationIdFormatter;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.process.IBpmScriptEngine;

/**
 * Adapter that listens for messages and then call the {@link IBpmScriptEngine} sendNext
 * method with the appropriate parameters.
 */
public class BpmScriptNextAdapter implements IMessageReceiver {

    private IBpmScriptEngine engine = null;
    private IMessageSender sender = null;
    private ICorrelationIdFormatter correlationIdFormatter  = new CorrelationIdFormatter();
    private String errorAddress;

    public BpmScriptNextAdapter() {
        super();
    }

    public BpmScriptNextAdapter(IBpmScriptEngine engine, IMessageSender sender, String errorAddress) {
        super();
        this.engine = engine;
        this.sender = sender;
        this.errorAddress = errorAddress;
    }

    /**
     * Unwraps the correlation id to find out the appropriate instance id, branch,
     * version and queueid to send the message to.
     */
    public void onMessage(IInternalMessage internalMessage) {
        try {
            ICorrelationId correlationId = correlationIdFormatter.parse(internalMessage.getCorrelationId());
            // TODO: also, we need to think about the version issue
            // what version issue? write more notes, idiot...
            // the version issue, as in what if we get a message back on a branch
            // before it splits (potentially... should have been called the branch issue)
            engine.sendNext(correlationId.getPid(), correlationId.getBranch(),
                    internalMessage, correlationId.getQueueId());
        } catch (BpmScriptException e) {
            String replyTo = errorAddress;
            if (internalMessage instanceof IInvocationMessage) {
                IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
                replyTo = invocationMessage.getReplyTo();
            }
            sender.send(replyTo, new ExceptionMessage(internalMessage.getCorrelationId(),
                    e));
        }
    }

    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

    public void setErrorAddress(String errorAddress) {
        this.errorAddress = errorAddress;
    }

}
