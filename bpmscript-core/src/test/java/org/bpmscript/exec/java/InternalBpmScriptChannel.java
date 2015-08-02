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

package org.bpmscript.exec.java;

import org.bpmscript.BpmScriptException;
import org.bpmscript.integration.Envelope;
import org.bpmscript.integration.internal.InvocationMessage;

/**
 * 
 */
public class InternalBpmScriptChannel {

    private IJavaChannel channel = null;

    public void send(long timeout, IJavaMessageHandler<?> handler, Object... args) throws BpmScriptException {
        InvocationMessage invocationMessage = new InvocationMessage();
        invocationMessage.setMethodName("sendFirst");
        invocationMessage.setArgs(args);
        Envelope envelope = new Envelope("bpmscript-first", invocationMessage);
        this.channel.send(envelope, timeout, handler);
    }
    public void reply(Object response) {}
}
