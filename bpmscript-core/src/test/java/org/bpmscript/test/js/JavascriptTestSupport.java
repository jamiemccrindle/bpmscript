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

package org.bpmscript.test.js;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bpmscript.js.DynamicContextFactory;
import org.bpmscript.js.Global;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.bpmscript.util.StreamService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public class JavascriptTestSupport implements ITestSupport<IServiceLookup> {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private Map<String, Object> variables = new HashMap<String, Object>();
    private String script;

    public JavascriptTestSupport(String script) {
        super();
        this.script = script;
    }

    public void execute(ITestCallback<IServiceLookup> callback) throws Exception {
        ServiceLookup lookup = new ServiceLookup();
        Context context = new DynamicContextFactory().enterContext();
        context.setLanguageVersion(Context.VERSION_1_7);
        try {
            ScriptableObject scope = new Global(context);
            Script compiledScript = context.compileString(StreamService.DEFAULT_INSTANCE.getResourceAsString(script),
                    script, 1, null);
            Set<Entry<String, Object>> entrySet = variables.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                ScriptableObject.putProperty(scope, entry.getKey(), Context.javaToJS(entry.getValue(), scope));
            }
            ScriptableObject.putProperty(scope, "log", Context.javaToJS(log, scope));
            lookup.addService("script", compiledScript);
            lookup.addService("scope", scope);
            lookup.addService("context", context);
            callback.execute(lookup);
        } finally {
            Context.exit();
        }
    }

    public void addVariable(String name, Object object) {
        this.variables.put(name, object);
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setScript(String script) {
        this.script = script;
    }

}
