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
import java.util.HashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.exec.UnserializeableObject;
import org.bpmscript.integration.Envelope;


/**
 * A Test Java Process
 */
@SuppressWarnings("serial")
public class TestProcess implements Serializable {
    private transient UnserializeableObject unserializeable = null; 
    public ProcessState test(final IJavaChannel channel, Object message) throws BpmScriptException {
        Envelope envelope = new Envelope("address", "blah");
        channel.send(envelope, 1000, new IJavaMessageHandler<Object>() {
            public ProcessState process(Object message) throws BpmScriptException {
                HashMap<String, Object> response = new HashMap<String, Object>();
                channel.reply(response);
                return COMPLETED;
            }
            public ProcessState timeout() throws BpmScriptException {
                throw new BpmScriptException("Timed out");
            }
        });
        return PAUSED;
    }
    public UnserializeableObject getUnserializeable() {
        return unserializeable;
    }
    public void setUnserializeable(UnserializeableObject unserializeable) {
        this.unserializeable = unserializeable;
    }
}
