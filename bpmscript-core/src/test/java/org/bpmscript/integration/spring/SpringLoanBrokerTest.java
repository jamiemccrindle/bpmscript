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

package org.bpmscript.integration.spring;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.loanbroker.LoanRequest;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.channel.ChannelRegistry;
import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.message.GenericMessage;

/**
 * Ok, here's the loan broker... I wonder why it's so slow...
 */
public class SpringLoanBrokerTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testReply() throws Exception {

        int total = 1;
        final CountDownLatch latch = new CountDownLatch(total);

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/integration/spring/spring.xml");

        try {

            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");
            final ChannelRegistry channelRegistry = (ChannelRegistry) ((Map) context
                    .getBeansOfType(ChannelRegistry.class)).values().iterator().next();

            processManager.createDefinition("id", new JavascriptProcessDefinition("loanBroker",
                    StreamService.DEFAULT_INSTANCE
                            .getResourceAsString("/org/bpmscript/integration/spring/loanbroker.js")));
            engine.setInstanceListener(new LoggingInstanceListener() {
                @Override
                public void instanceCompleted(String pid, ICompletedResult result) {
                    super.instanceCompleted(pid, result);
                    latch.countDown();
                }

                @Override
                public void instanceFailed(String pid, IFailedResult result) {
                    super.instanceFailed(pid, result);
                    fail(result.getThrowable().getMessage());
                }
            });

            IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                @SuppressWarnings("unchecked")
                public void execute(int count) throws Exception {
                    GenericMessage<Object[]> message = new GenericMessage<Object[]>(new Object[] { new LoanRequest(
                            "asdf", 1, 1000) });
                    message.getHeader().setAttribute("definitionName", "loanBroker");
                    message.getHeader().setAttribute("operation", "requestBestRate");
                    message.getHeader().setReturnAddress("channel-recorder");
                    MessageChannel channel = channelRegistry.lookupChannel("channel-bpmscript-first");
                    channel.send(message);
                }
            }, new IWaitForCallback() {
                public void call() throws Exception {
                    latch.await();
                }
            }, false));

            SpringRecorder springRecorder = (SpringRecorder) context.getBean("springRecorder");
            BlockingQueue<Object> messages = springRecorder.getMessages();
            for (int i = 0; i < total; i++) {
                Object poll = messages.poll(1, TimeUnit.SECONDS);
                assertNotNull("should have got to " + total + " but got to " + i, poll);
            }

        } finally {
            context.destroy();
        }

    }
}
