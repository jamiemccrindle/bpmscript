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

package org.bpmscript.exec.js;

import org.bpmscript.BpmScriptException;
import org.bpmscript.channel.IConverter;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.integration.internal.JsConverter;
import org.bpmscript.js.MapToJsConverter;
import org.bpmscript.process.IDefinitionConfiguration;
import org.mozilla.javascript.Scriptable;

/**
 * A Decorating {@link IScriptChannel} that converts Javascript Objects to
 * Maps, Arrays and regular Java Objects.
 */
public class ConvertingScriptChannel implements IScriptChannel {
    
    private IScriptChannel delegate;
    private IConverter converter = new JsConverter();
    private MapToJsConverter mapToJsConverter = new MapToJsConverter();

    public ConvertingScriptChannel(IScriptChannel delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * Simply delegates
     */
    public Object getContent(Object message) {
        return delegate.getContent(message);
    }

    /**
     * Simply delegates
     */
    public Object getContent(Scriptable scope, Object message) {
        return mapToJsConverter.convertObject(scope, delegate.getContent(message));
    }

    /**
     * Simply delegates
     */
    public void reply(String pid, Object in, Object out) throws BpmScriptException {
        delegate.reply(pid, in, converter.convert(out));
    }

    /**
     * Converts and delegates
     */
    public void send(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        delegate.send(fromPid, fromBranch, fromVersion, queueId, converter.convert(message));
    }

    /**
     * Converts and delegates
     */
    public void sendOneWay(Object message) throws BpmScriptException {
        delegate.sendOneWay(converter.convert(message));
    }

    /**
     * Simply delegates
     */
    public void sendTimeout(String fromPid, String fromBranch, String fromVersion, String queueId, long duration)
            throws BpmScriptException {
        delegate.sendTimeout(fromPid, fromBranch, fromVersion, queueId, duration);
    }

    /**
     * Simply delegates
     */
    public IDefinitionConfiguration getDefinitionConfiguration(String definitionName) {
        return delegate.getDefinitionConfiguration(definitionName);
    }

    public Object createCallback(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
            throws BpmScriptException {
        return delegate.createCallback(fromPid, fromBranch, fromVersion, queueId, converter.convert(message));
    } 

}
