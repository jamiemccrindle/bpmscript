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

package org.bpmscript.exec.js.serialize;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import org.bpmscript.IPausedResult;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.exec.js.scope.memory.MemoryScopeStore;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.memory.MemoryIntegrationTestSupport;
import org.bpmscript.integration.internal.memory.MemoryMessageBus;
import org.bpmscript.journal.memory.MemoryContinuationJournal;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.util.StreamService;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.debug.DebuggableScript;

/**
 * 
 */
public class ContinuationInspectionTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    public void testChannel() throws Exception {

        MemoryIntegrationTestSupport integrationTestSupport = new MemoryIntegrationTestSupport();
        integrationTestSupport.execute(new ITestCallback<IServiceLookup>() {

            public void execute(IServiceLookup services) throws Exception {
                final CountDownLatch latch = new CountDownLatch(1);

                final MemoryMessageBus bus = services.get("bus");
                final BpmScriptEngine engine = services.get("engine");
                final MemoryScopeStore scopeStore = services.get("scopeStore");
                final JavascriptSerializingContinuationService continuationService = services.get("continuationService");
                final MemoryContinuationJournal continuationJournal = services.get("continuationJournal");
                final AtomicReference<Continuation> continuationReference = new AtomicReference<Continuation>();

                IVersionedDefinitionManager processManager = engine.getVersionedDefinitionManager();
                processManager.createDefinition("id", new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE
                        .getResourceAsString("/org/bpmscript/integration/memory/reply.js")));
                engine.setInstanceListener(new LoggingInstanceListener() {
                    @Override
                    public void instancePaused(String pid, IPausedResult result) {
                        continuationReference.set((Continuation) result.getContinuation());
                        latch.countDown();
                    }
                });

                InvocationMessage message = new InvocationMessage();
                message.setArgs(null, "test", "test", new Object[] { new Integer(1) });
                message.setReplyTo("recorder");
                bus.send("bpmscript-first", message);
                latch.await();
                
                Continuation continuation = continuationReference.get();
                Object implementation = continuation.getImplementation();
                Field[] fields = implementation.getClass().getFields();
                // Field field = implementation.getClass().getField("idata");
                Field declaredField = implementation.getClass().getDeclaredField("idata");
                declaredField.setAccessible(true);
                Object object = declaredField.get(implementation);
                DebuggableScript debuggableScript = (DebuggableScript) object;
                
                printStuff(debuggableScript);
                
//                DynamicContextFactory dynamicContextFactory = new DynamicContextFactory();
//                dynamicContextFactory.addListener(new ContextFactory.Listener() {
//                
//                    public void contextReleased(Context cx) {
//                        // TODO Auto-generated method stub
//                
//                    }
//                
//                    public void contextCreated(Context cx) {
//                        // TODO Auto-generated method stub
//                
//                    }
//                
//                });
//                Context cx = dynamicContextFactory.enterContext();
//                cx.setOptimizationLevel(-1);
//                cx.setDebugger(new Debugger() {
//                
//                    public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
//                        log.info("here");
//                    }
//                
//                    public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
//                        log.info("here");
//                        return new DebugFrame() {
//                        
//                            public void onLineChange(Context cx, int lineNumber) {
//                                log.info("onLineChange");
//                            }
//                        
//                            public void onExit(Context cx, boolean byThrow, Object resultOrException) {
//                                log.info("onExit");
//                            }
//                        
//                            public void onExceptionThrown(Context cx, Throwable ex) {
//                                log.info("onLineChange");
//                            }
//                        
//                            public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
//                                log.info("onEnter");
//                            }
//                        
//                            public void onDebuggerStatement(Context cx) {
//                                log.info("onDebuggerStatement");
//                            }
//                        
//                        };
//                    }
//                
//                }, new Object());
//                cx.setInstructionObserverThreshold(0);
//                
//                try {
//                    Scriptable scope = scopeStore.findScope(cx, "id");
//                    // // TODO: hang on, why do we pass the invocation context? hmmm... do we
//                    // restore it in the get
//                    // continuationService.getContinuation(new HashMap<String, Object>(), scope,
//                    // pid, branch);
//                    Object call = continuationReference.get().call(cx, scope, scope,
//                            new Object[] { Context.javaToJS(new NextMessage("", "", "", "", "debug"), scope) });
//                    assertNotNull(call);
//                } finally {
//                    Context.exit();
//                }

            }

            /**
             * @param debuggableScript
             */
            private void printStuff(DebuggableScript debuggableScript) {
                log.debug("sourceName " + debuggableScript.getSourceName());
                log.debug("name " + debuggableScript.getFunctionName());
                int functionCount = debuggableScript.getFunctionCount();
                for(int i = 0; i < functionCount; i++) {
                    log.debug("function " + debuggableScript.getFunction(i).getFunctionName());
                }
                int[] lineNumbers = debuggableScript.getLineNumbers();
                for (int j : lineNumbers) {
                    log.debug("line number: " + j);
                }
                int paramAndVarCount = debuggableScript.getParamAndVarCount();
                for(int i = 0; i < paramAndVarCount; i++) {
                    log.debug("param or var name " + debuggableScript.getParamOrVarName(i));
                }
                DebuggableScript parent = debuggableScript.getParent();
                if(parent != null) {
                    printStuff(parent);
                }
            }

        });

    }
}
