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

import java.util.Arrays;

import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.InvocationMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Receives messages from the bus that contain information about which method
 * to call on a bean in the spring application context as well as the arguments
 * to pass to it.
 */
public class SpringAdapter implements ApplicationContextAware, IMessageReceiver {

    private ApplicationContext applicationContext;

    private IMessageSender sender;

    public SpringAdapter() {
        super();
    }

    public SpringAdapter(ApplicationContext applicationContext, IMessageSender sender) {
        super();
        this.applicationContext = applicationContext;
        this.sender = sender;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Receive a message that contains a bean name, methodName and arguments. It then
     * looks up the bean name in the application context, looks up the appropriate
     * method and then calls it with the arguments from the internal message. 
     * 
     * Does not handle overloading.
     */
    @SuppressWarnings("unchecked")
    public void onMessage(IInternalMessage internalMessage) {
        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            try {
                Object[] args = invocationMessage.getArgs();
                String name = (String) args[0];
                Object bean = applicationContext.getBean(name);
                ReflectionAdapter reflectionAdapter = new ReflectionAdapter(sender, bean);
                InvocationMessage reflectionMessage = new InvocationMessage();
                if(args.length > 1) {
                    reflectionMessage.setArgs(Arrays.asList(args).subList(1, args.length).toArray());
                } else {
                    reflectionMessage.setArgs(new Object[] {});
                }
                reflectionMessage.setCorrelationId(invocationMessage.getCorrelationId());
                reflectionMessage.setMethodName(invocationMessage.getMethodName());
                reflectionMessage.setReplyTo(invocationMessage.getReplyTo());
                reflectionMessage.setSignature(invocationMessage.getSignature());
                reflectionAdapter.onMessage(reflectionMessage);
            } catch (final Throwable e) {
                sender.send(invocationMessage.getReplyTo(), new ExceptionMessage(invocationMessage.getCorrelationId(),
                        e));
            }
        }
    }

}
