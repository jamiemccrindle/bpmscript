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

import java.util.concurrent.ConcurrentHashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.exec.js.scope.hibernate.HibernateScopeStore;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Caching scope store. Useful if the downstream scope store takes a while e.g. if it is back by a
 * database. This intended to act as a decorator, i.e. a delegate needs to be set.
 * 
 * @see HibernateScopeStore
 */
public class CachingScopeStore implements IScopeStore {

    private ConcurrentHashMap<String, Scriptable> scopeCache = new ConcurrentHashMap<String, Scriptable>();
    private IScopeStore delegate;

    public CachingScopeStore() {
    }

    /**
     * Create a caching scope with a delegate
     * 
     * @param delegate the delegate that will have calls forwarded to it in the case of cache misses
     */
    public CachingScopeStore(IScopeStore delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * @see IScopeStore#findScope(Context, String)
     */
    public Scriptable findScope(Context cx, String processId) throws BpmScriptException {
        Scriptable scriptable = scopeCache.get(processId);
        if (scriptable == null) {
            synchronized (scopeCache) {
                scriptable = scopeCache.get(processId);
                if (scriptable == null) {
                    scriptable = delegate.findScope(cx, processId);
                    if(scriptable != null) {
                        scopeCache.put(processId, scriptable);
                    }
                }
            }
        }
        return scriptable;
    }

    /**
     * @see IScopeStore#storeScope(Context, String, Scriptable)
     */
    public void storeScope(Context cx, String processId, Scriptable scope) throws BpmScriptException {
        synchronized (scopeCache) {
            scopeCache.put(processId, scope);
        }
        delegate.storeScope(cx, processId, scope);
    }

    public void setDelegate(IScopeStore delegate) {
        this.delegate = delegate;
    }
}
