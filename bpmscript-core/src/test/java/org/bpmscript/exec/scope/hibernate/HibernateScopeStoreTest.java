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

package org.bpmscript.exec.scope.hibernate;

import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.bpmscript.BpmScriptException;
import org.bpmscript.exec.js.scope.hibernate.HibernateScope;
import org.bpmscript.exec.js.scope.hibernate.HibernateScopeStore;
import org.bpmscript.js.DynamicContextFactory;
import org.bpmscript.js.Global;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.bpmscript.test.hibernate.SpringSessionFactoryTestSupport;
import org.hibernate.SessionFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * 
 */
public class HibernateScopeStoreTest extends TestCase {

    public static class HibernateScopeStoreTestSupport implements ITestSupport<IServiceLookup> {

        /*
         * (non-Javadoc)
         * 
         * @see org.bpmscript.test.ITestSupport#execute(org.bpmscript.test.ITestCallback)
         */
        public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
            SpringSessionFactoryTestSupport sessionFactoryTestSupport = new SpringSessionFactoryTestSupport(
                    HibernateScope.class);
            sessionFactoryTestSupport.execute(new ITestCallback<IServiceLookup>() {
                public void execute(IServiceLookup services) throws Exception {
                    HibernateScopeStore scopeStore = new HibernateScopeStore();
                    scopeStore.setSessionFactory((SessionFactory) services.get("sessionFactory"));
                    scopeStore.afterPropertiesSet();
                    ServiceLookup serviceLookup = new ServiceLookup(services);
                    serviceLookup.addService("scopeStore", scopeStore);
                    Context cx = new DynamicContextFactory().enterContext();
                    cx.setOptimizationLevel(-1);
                    serviceLookup.addService("cx", cx);
                    try {
                        callback.execute(serviceLookup);
                    } finally {
                        Context.exit();
                    }
                }
            });
        }

    }

    /**
     * Test method for
     * {@link org.bpmscript.exec.js.scope.hibernate.HibernateScopeStore#findScope(org.mozilla.javascript.Context, java.lang.String)}.
     * 
     * @throws Exception
     */
    public void testFindScope() throws Exception {
        HibernateScopeStoreTestSupport testSupport = new HibernateScopeStoreTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateScopeStore scopeStore = services.get("scopeStore");
                Context cx = services.get("cx");
                Global scope = new Global(cx);
                Script script = cx.compileString("function Test() { return 'Hello World!'; }", "test", 0, null);
                Stack<String> sourceStack = new Stack<String>();
                sourceStack.push("test");
                cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
                cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, new LinkedBlockingQueue<String>());
                script.exec(cx, scope);
                String definitionId = "definitionId";
                scopeStore.storeScope(cx, definitionId, scope);
                Scriptable result = scopeStore.findScope(cx, definitionId);
                assertNotNull(result);
                // should throw an exception
                Scriptable noScope = scopeStore.findScope(cx, "nothere");
                assertNull(noScope);
            }
        });
    }

    /**
     * Test method for
     * {@link org.bpmscript.exec.js.scope.hibernate.HibernateScopeStore#storeScope(org.mozilla.javascript.Context, java.lang.String, org.mozilla.javascript.Scriptable)}.
     * 
     * @throws Exception
     */
    public void testStoreScope() throws Exception {
        HibernateScopeStoreTestSupport testSupport = new HibernateScopeStoreTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateScopeStore scopeStore = services.get("scopeStore");
                Context cx = services.get("cx");
                Global scope = new Global(cx);
                Script script = cx.compileString("function Test() { return 'Hello World!'; }", "test", 0, null);
                Stack<String> sourceStack = new Stack<String>();
                sourceStack.push("test");
                cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
                cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, new LinkedBlockingQueue<String>());
                script.exec(cx, scope);
                String definitionId = "definitionId";
                scopeStore.storeScope(cx, definitionId, scope);
                Scriptable result = scopeStore.findScope(cx, definitionId);
                assertNotNull(result);
            }
        });
    }

}
