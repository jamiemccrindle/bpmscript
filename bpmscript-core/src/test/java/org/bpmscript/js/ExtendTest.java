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

package org.bpmscript.js;

import junit.framework.TestCase;

import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.js.JavascriptTestSupport;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * 
 */
public class ExtendTest extends TestCase {
    public void testExtend() throws Exception {
        JavascriptTestSupport testSupport = new JavascriptTestSupport("/org/bpmscript/js/extend.js");
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                Context context = services.get("context");
                Script script = services.get("script");
                Scriptable scope = services.get("scope");
                Object result = script.exec(context, scope);
                assertNotNull(result);
            }
        });
    }
}
