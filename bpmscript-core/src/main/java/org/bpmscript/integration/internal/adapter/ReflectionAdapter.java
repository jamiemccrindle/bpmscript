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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.bpmscript.BpmScriptException;
import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.ResponseMessage;

/**
 * Adapter that takes messages and reflectively calls the appropriate method on
 * a downstream object based on the content of the incoming message.
 */
public class ReflectionAdapter implements IMessageReceiver {

    private static final Object[] EMPTY_ARRAY = {};
    private Object delegate;
    private IMessageSender sender;

    public ReflectionAdapter(IMessageSender sender, Object delegate) {
        super();
        this.sender = sender;
        this.delegate = delegate;
    }

    /**
     * Upon receipt of a message query it for which method should be called on
     * the object set on this adapter. If the method is found, it is invoked with
     * the arguments supplied in the method. 
     * 
     * Does not deal with overloaded methods.
     */
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            try {
                final String methodName = invocationMessage.getMethodName();
                Object[] arguments = (Object[]) invocationMessage.getArgs();
                if (arguments == null) {
                    arguments = EMPTY_ARRAY;
                }
                final AtomicReference<Method> found = new AtomicReference<Method>(null);
                Method[] methods = delegate.getClass().getMethods();
                // could be safer and faster
                for (Method method : methods) {
                    if (method.getName().equals(methodName) && method.getParameterTypes().length == arguments.length) {
                        found.set(method);
                        break;
                    }
                }
                if (found.get() != null) {
                    try {
                        Object result = found.get().invoke(delegate, arguments);
                        sender.send(invocationMessage.getReplyTo(), new ResponseMessage(internalMessage.getCorrelationId(),
                                result));
                    } catch (IllegalArgumentException e) {
                        sender
                                .send(invocationMessage.getReplyTo(), new ExceptionMessage(internalMessage.getCorrelationId(),
                                        e));
                    } catch (IllegalAccessException e) {
                        sender
                                .send(invocationMessage.getReplyTo(), new ExceptionMessage(internalMessage.getCorrelationId(),
                                        e));
                    } catch (InvocationTargetException e) {
                        sender
                                .send(invocationMessage.getReplyTo(), new ExceptionMessage(internalMessage.getCorrelationId(),
                                        e));
                    }
                } else {
                    sender.send(invocationMessage.getReplyTo(), new ExceptionMessage(invocationMessage
                            .getCorrelationId(), new BpmScriptException("could not find method with name " + methodName
                            + " and arguments " + arguments)));
                }
            } catch (final Throwable e) {
                sender.send(invocationMessage.getReplyTo(), new ExceptionMessage(internalMessage.getCorrelationId(), e));
            }
        }
    }

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

}
