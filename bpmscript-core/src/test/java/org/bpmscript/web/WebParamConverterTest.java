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

package org.bpmscript.web;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.js.Global;
import org.bpmscript.util.StreamService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public class WebParamConverterTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    public void testConvert() throws Exception {
        final Map<String, String[]> params = getParams();
        final WebParamConverter converter = new WebParamConverter();
        IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(10, new IBenchmarkCallback() {
            public void execute(int count) throws Exception {
                converter.convert(params);
            }
        }));
    }

    /**
     * @return
     */
    private Map<String, String[]> getParams() {
        final Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("customer.addresses[0].houseNumber", new String[] {"20b"});
        params.put("customer.addresses[0].street", new String[] {"Norway Gate"});
        params.put("customer.addresses[0].postCode", new String[] {"SE165BT"});
        params.put("customer.addresses[1].houseNumber", new String[] {"20b"});
        params.put("customer.addresses[1].street", new String[] {"Norway Gate"});
        params.put("customer.addresses[1].postCode", new String[] {"SE165BT"});
        params.put("customer.gender", new String[] {"Male"});
        params.put("customer.dob", new String[] {"23/7/2008"});
        params.put("customer.interests[]", new String[] {"reading", "sleeping", "watching tv"});
        return params;
    }
    
    public void testMapping() throws Exception {
        Context context = new ContextFactory().enterContext();
        try {
            String script = "/org/bpmscript/web/webconvert.js";
            ScriptableObject scope = new Global(context);
            Script compiledScript = context.compileString(StreamService.DEFAULT_INSTANCE.getResourceAsString(script), script, 1, null);
            WebParamConverter converter = new WebParamConverter();
            ScriptableObject.putProperty(scope, "log", Context
                    .javaToJS(log, scope));
            ScriptableObject.putProperty(scope, "converter", Context
                    .javaToJS(converter, scope));
            NativeArray array = new NativeArray(0);
            array.setParentScope(scope);
            NativeObject object = new NativeObject();
            object.setParentScope(scope);
            ScriptableObject.putProperty(array, 0, "Hello World!");
            ScriptableObject.putProperty(scope, "array", array);
            ScriptableObject.putProperty(scope, "params", Context
                    .javaToJS(getParams(), scope));
            Object result = compiledScript.exec(context, scope);
            assertNotNull(result);
        } finally {
            Context.exit();
        }
    }
}
