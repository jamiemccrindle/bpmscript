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

import static org.bpmscript.ProcessState.COMPLETED;
import static org.bpmscript.ProcessState.PAUSED;

import java.io.Serializable;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.integration.Envelope;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IResponseMessage;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.ResponseMessage;


/**
 * ReplyProcess
 */
@SuppressWarnings("serial")
public class ReplyProcess implements Serializable {
    public ProcessState test(final IJavaChannel channel, Object message) throws BpmScriptException {
        InvocationMessage invocationMessage = new InvocationMessage();
        invocationMessage.setArgs(channel.getParentVersion(), "test", "sub", new Object[] {"amessage"});
        Envelope envelope = new Envelope("bpmscript-first", invocationMessage);
        channel.send(envelope, 1000, new IJavaMessageHandler<IInternalMessage>() {
            public ProcessState process(IInternalMessage message) throws BpmScriptException {
                ResponseMessage response = new ResponseMessage();
                response.setContent(((IResponseMessage) message).getContent());
                channel.reply(response);
                return COMPLETED;
            }
            public ProcessState timeout() throws BpmScriptException {
                throw new BpmScriptException("Timed out");
            }
        });
        return PAUSED;
    }
    public ProcessState sub(final IJavaChannel channel, Object message) throws BpmScriptException {
        ResponseMessage response = new ResponseMessage();
        response.setContent("hello");
        channel.reply(response);
        return COMPLETED;
    }
}
