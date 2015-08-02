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
import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.process.IBpmScriptEngine;

/**
 * An internal adapter that listens for messages and passes them on to
 * the "sendFirst" method of an {@link IBpmScriptEngine}
 */
public class BpmScriptFirstAdapter implements IMessageReceiver {

    private IBpmScriptEngine engine = null;
    private IMessageSender sender = null;

    public BpmScriptFirstAdapter() {
        super();
    }

    /**
     * Construct the adapter with the engine it will pass messages to 
     * and the sender it will use to send error messages
     */
    public BpmScriptFirstAdapter(IBpmScriptEngine engine, IMessageSender sender) {
        super();
        this.engine = engine;
        this.sender = sender;
    }

    /**
     * Receives an internal message, expects it to be an {@link IInvocationMessage} and 
     * interrogates it for the parameters to pass to the {@link IBpmScriptEngine} sendFirst
     * method. Creates a new message to pass into the sendFirst method. The arguments 
     * expected in the internal message the same as those on sendFirst
     * 
     * Expects the following arguments to be inside the internalMessage argument.
     * 
     * <ul>
     * <li>parentVersion</li>
     * <li>definitionName</li>
     * <li>operation</li>
     * <li>message</li>
     * </ul>
     * 
     * @see IBpmScriptEngine#sendFirst(String, String, String, Object)
     */
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            try {
                Object[] args = invocationMessage.getArgs();
                InvocationMessage processMessage = new InvocationMessage();
                processMessage.setMessageId(internalMessage.getMessageId());
                processMessage.setCorrelationId(invocationMessage.getCorrelationId());
                processMessage.setReplyTo(invocationMessage.getReplyTo());
                processMessage.setMethodName((String) args[2]);
                processMessage.setArgs((Object[]) args[3]);
                engine.sendFirst((String) args[0], (String) args[1], (String) args[2], processMessage);
            } catch (BpmScriptException e) {
                sender.send(invocationMessage.getReplyTo(), new ExceptionMessage(invocationMessage.getCorrelationId(),
                        e));
            }
        }
    }

    /**
     * @param engine the BpmScript engine to pass messages to
     */
    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * @param sender the sender used to reply with error if there is a problem
     */
    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

}
