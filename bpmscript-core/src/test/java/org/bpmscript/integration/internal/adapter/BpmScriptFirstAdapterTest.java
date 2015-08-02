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
import org.bpmscript.IProcessDefinition;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.process.IBpmScriptEngine;

import junit.framework.TestCase;

/**
 * Tests the BpmScriptFirst Adapter
 */
public class BpmScriptFirstAdapterTest extends TestCase {
    public void testBpmScriptFirstAdapter() throws Exception {
        BpmScriptFirstAdapter adapter = new BpmScriptFirstAdapter();
        adapter.setEngine(new IBpmScriptEngine() {
            public String validate(IProcessDefinition processDefinition) throws BpmScriptException {
                fail("should not call validate");
                return null;
            }
            public void sendNext(String pid, String branch, Object message, String queueId) throws BpmScriptException {
                fail("should not call sendNext");
            }
            public void sendFirst(String parentVersion, String definitionName, String operation, Object message)
                    throws BpmScriptException {
                assertEquals(null, parentVersion);
                assertEquals("test", definitionName);
                assertEquals("testoperation", operation);
            }
            public void kill(String pid, String branch, String message) throws BpmScriptException {
                fail("should not call kill");
            }
        });
        adapter.setSender(new IMessageSender() {
            public void send(String address, IInternalMessage internalMessage) {
                fail("should not call send");
            }
        });
        InvocationMessage internalMessage = new InvocationMessage();
        internalMessage.setArgs(null, "test", "testoperation", new Object[] {"Hello World!"});
        adapter.onMessage(internalMessage);
    }
}
