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

package org.bpmscript.exec.js.scope.memory;

import java.util.concurrent.ConcurrentHashMap;

import org.bpmscript.exec.js.scope.IScopeStore;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Stores scopes in memory
 */
public class MemoryScopeStore implements IScopeStore {

    private ConcurrentHashMap<String, Scriptable> scopeStore = new ConcurrentHashMap<String, Scriptable>();
    
    /**
     * @see org.bpmscript.exec.js.scope.IScopeStore#findScope(java.lang.String)
     */
    public Scriptable findScope(Context cx, String processId) {
        return scopeStore.get(processId);
    }

    /**
     * @see org.bpmscript.exec.js.scope.IScopeStore#storeScope(java.lang.String, org.mozilla.javascript.Scriptable)
     */
    public void storeScope(Context cx, String processId, Scriptable scope) {
        scopeStore.put(processId, scope);
    }

}
