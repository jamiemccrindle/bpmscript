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

import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.InvocationMessage;

import junit.framework.TestCase;

/**
 * Conversation correlator adapter test
 */
public class ConversationCorrelatorAdapterTest extends TestCase {
    public void testAdapter() throws Exception {
        ConversationCorrelatorAdapter adapter = new ConversationCorrelatorAdapter();
        adapter.setSender(new IMessageSender() {
            public void send(String address, IInternalMessage internalMessage) {
                assertTrue(internalMessage instanceof IResponseMessage);
                IResponseMessage responseMessage = (IResponseMessage) internalMessage;
                Object content = responseMessage.getContent();
                assertTrue(content instanceof IInvocationMessage);
                IInvocationMessage invocationMessage = (IInvocationMessage) content;
                assertEquals("Hello World!", invocationMessage.getArgs()[0]);
                assertEquals("test", responseMessage.getCorrelationId());
            }
        });
        Object[] args = new Object[] {"bpmscript-next", "test", "Hello World!"};
        InvocationMessage internalMessage = new InvocationMessage();
        internalMessage.setArgs(args);
        adapter.onMessage(internalMessage);
    }
}
