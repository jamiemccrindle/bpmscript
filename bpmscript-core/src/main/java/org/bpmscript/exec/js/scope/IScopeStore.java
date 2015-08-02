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

package org.bpmscript.exec.js.scope;

import org.bpmscript.BpmScriptException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Stores and retrieves scopes for definitions
 */
public interface IScopeStore {
    /**
     * Store the scope for a particular definition
     * 
     * @param cx the JavaScript context
     * @param definitionId the definionId. must not be null 
     * @param scope the scope for this definition
     * @throws BpmScriptException if there was a problem storing the definition scope
     */
    void storeScope(Context cx, String definitionId, Scriptable scope) throws BpmScriptException;
    
    /**
     * Find the scope for a particular definition
     *  
     * @param cx the JavaScript context to use
     * @param definitionId the definitionId for which you require the scope. must not be null.
     * @return the scope associated with the definition, null if it couldn't be found.
     * @throws BpmScriptException if there was a problem putting it together
     */
    Scriptable findScope(Context cx, String definitionId) throws BpmScriptException;
}
