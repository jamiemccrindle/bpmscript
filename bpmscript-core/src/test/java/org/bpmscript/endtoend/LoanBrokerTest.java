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

package org.bpmscript.endtoend;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.adapter.RecordingAdapter;
import org.bpmscript.loanbroker.LoanRequest;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test loan broker end to end using the internal integration
 */
public class LoanBrokerTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public void testLoanBroker() throws Exception {
        final int total = 10;

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/endtoend/spring.xml");

        try {

            final IMessageSender bus = (IMessageSender) context.getBean("bus");
            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");

            String source = StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/endtoend/loanbroker.js");
            String id = engine.validate(new JavascriptProcessDefinition("loanBroker", source));

            processManager.createDefinition(id, new JavascriptProcessDefinition("loanBroker", source));

            IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                public void execute(int count) throws Exception {
                    InvocationMessage message = new InvocationMessage();
                    message.setArgs(null, "loanBroker", "requestBestRate", new Object[] { new LoanRequest("asdf", 1,
                            1000) });
                    message.setReplyTo("recording");
                    bus.send("bpmscript-first", message);
                }
            }, new IWaitForCallback() {
                public void call() throws Exception {
                    RecordingAdapter recorder = (RecordingAdapter) context.getBean("recording");
                    BlockingQueue<IInternalMessage> internalMessages = recorder.getMessages();
                    for (int i = 0; i < total; i++) {
                        Object result = internalMessages.poll(2, TimeUnit.SECONDS);
                        assertNotNull(result);
                    }
                }

            }, false));

        } finally {
            context.destroy();
        }
        log.info("done");
    }

    public static void main(String[] args) throws Exception {
        LoanBrokerTest test = new LoanBrokerTest();
        test.testLoanBroker();
    }

}
