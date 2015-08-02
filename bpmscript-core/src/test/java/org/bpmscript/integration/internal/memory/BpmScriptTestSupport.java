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

package org.bpmscript.integration.internal.memory;

import java.util.concurrent.CountDownLatch;

import org.bpmscript.ICompletedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.util.StreamService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 */
public class BpmScriptTestSupport implements ITestSupport<ApplicationContext> {

    private final int total;
    private final String spring;
    private final String script;

    public BpmScriptTestSupport(int total, String spring, String script) {
        this.total = total;
        this.spring = spring;
        this.script = script;
    }

    /**
     * @see org.bpmscript.test.ITestSupport#execute(org.bpmscript.test.ITestCallback)
     */
    public void execute(final ITestCallback<ApplicationContext> callback) throws Exception {
        final CountDownLatch latch = new CountDownLatch(total);
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(spring);
        try {
            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");
            processManager.createDefinition("id", new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE.getResourceAsString(script)));
            engine.setInstanceListener(new LoggingInstanceListener() {
                @Override
                public void instanceCompleted(String pid, ICompletedResult result) {
                    super.instanceCompleted(pid, result);
                    latch.countDown();
                }
            });
            IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                public void execute(int count) throws Exception {
                    callback.execute(context);
                }
            }, new IWaitForCallback() {
                public void call() throws Exception {
                    latch.await();
                }
            }, false));
        } finally {
            context.destroy();
        }
    }

}
