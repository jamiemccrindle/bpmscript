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

import org.mozilla.javascript.Script;

/**
 * A cache of compiled scripts.
 */
public interface IJavascriptSourceCache {

    /**
     * Get a script taking in the source of the script and its name
     *  
     * @param source the source of the script
     * @param name the name of the source
     * @return a compiled script
     * @throws IOException if the script could not be loaded / compiled
     */
    Script getScript(String source, String name) throws IOException;
    
    /**
     * Clear the cache
     */
    void clearCache();
}