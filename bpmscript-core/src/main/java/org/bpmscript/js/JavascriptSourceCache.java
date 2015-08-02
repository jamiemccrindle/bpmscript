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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bpmscript.js.reload.ILibraryToFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.springframework.beans.factory.InitializingBean;

/**
 * The Javascript source cache implementation. Backs onto a {@link WeakHashMap} to stop out
 * of memory exceptions.
 */
public class JavascriptSourceCache implements IJavascriptSourceCache {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private Map<String, Script> cache = Collections
            .synchronizedMap(new WeakHashMap<String, Script>());

    /**
     * Get a compiled script either by looking it up in the source cache or compiling it
     * if it is not in the cache.
     */
    public Script getScript(String source, String name) throws IOException {
        Script script = cache.get(source);
        Context cx = new DynamicContextFactory().enterContext();
        cx.setLanguageVersion(Context.VERSION_1_7);
        try {
            if (script == null) {
                script = cx.compileReader(new StringReader(source), name, 0,
                        null);
                cache.put(source, script);
            }
            return script;
        } catch (EvaluatorException e) {
            String message = e.sourceName() + ":line " + 
                    e.lineNumber() + " col " + e.columnNumber()
                    + " " + e.lineSource() + " " + e.details();
            log.warn(message);
            throw new RuntimeException(message, e);
        } catch (Throwable e) {
            throw new RuntimeException(name + ": " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    /**
     * @see org.bpmscript.js.IJavascriptSourceCache#invalidateEntry(java.lang.String)
     */
    public void clearCache() {
        cache.clear();
    }


}
