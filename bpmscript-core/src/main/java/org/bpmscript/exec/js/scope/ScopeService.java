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

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.exec.js.IScopeService;
import org.bpmscript.exec.js.scope.memory.MemoryScopeStore;
import org.bpmscript.js.Global;
import org.bpmscript.js.reload.ILibraryToFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A Scope Service that creates a scope based on an incoming Javascript process
 * definition. 
 * 
 * The Scope Service is also responsible for notifying any listeners of any additional
 * files loaded in the process.
 * 
 * The Scope Service uses a backing ScopeStore to store historical scopes. This is 
 * so that historical scopes are not lost.
 */
public class ScopeService implements IScopeService {

    private IScopeStore scopeStore = new MemoryScopeStore();
    private BlockingQueue<ILibraryToFile> libraryAssociationQueue;

    /**
     * @see org.bpmscript.exec.IScopeService#createScope(java.lang.String,
     *      org.bpmscript.IProcessDefinition, java.util.Map, org.mozilla.javascript.Context)
     */
    public Scriptable createScope(String processId, IProcessDefinition processDefinition,
            Map<String, Object> invocationContext, Context cx) throws IOException, BpmScriptException {
        
        IJavascriptProcessDefinition javascriptProcessDefinition = (IJavascriptProcessDefinition) processDefinition;
        
        Scriptable scope = null;
        
        synchronized(scopeStore) {
            scope = scopeStore.findScope(cx, processId);
            if(scope == null) {
                scope = new Global(cx);
                Script script = cx.compileString(javascriptProcessDefinition.getSource(), processDefinition.getName(), 0, null);
                Stack<String> sourceStack = new Stack<String>();
                sourceStack.push(processDefinition.getName());
                cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
                if (libraryAssociationQueue != null) {
                    cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraryAssociationQueue);
                }
                script.exec(cx, scope);
                scopeStore.storeScope(cx, processId, scope);
            }
        }

        Scriptable threadScope = cx.newObject(scope);
        threadScope.setPrototype(scope);
        threadScope.setParentScope(null);

        if (invocationContext != null) {
            for (Map.Entry<String, Object> entry : invocationContext.entrySet()) {
                ScriptableObject.putProperty(threadScope, entry.getKey(), Context.javaToJS(entry.getValue(),
                        threadScope));
            }
        }

        return threadScope;
    }

    /**
     * @see org.bpmscript.exec.IScopeService#findScope(java.lang.String, java.util.Map,
     *      org.mozilla.javascript.Context)
     */
    public Scriptable findScope(String processId, Map<String, Object> invocationContext, Context cx)
            throws IOException, BpmScriptException {
        Scriptable scope = scopeStore.findScope(cx, processId);
        Scriptable threadScope = cx.newObject(scope);
        threadScope.setPrototype(scope);
        threadScope.setParentScope(null);

        if (invocationContext != null) {
            for (Map.Entry<String, Object> entry : invocationContext.entrySet()) {
                ScriptableObject.putProperty(threadScope, entry.getKey(), Context.javaToJS(entry.getValue(),
                        threadScope));
            }
        }
        return threadScope;
    }

    public void setLibraryAssociationQueue(BlockingQueue<ILibraryToFile> libraryAssociationQueue) {
        this.libraryAssociationQueue = libraryAssociationQueue;
    }

    public void setScopeStore(IScopeStore scopeStore) {
        this.scopeStore = scopeStore;
    }

}
