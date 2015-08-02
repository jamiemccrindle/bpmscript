package org.bpmscript.exec.java;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.integration.internal.ExceptionMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.adapter.RecordingAdapter;
import org.bpmscript.integration.internal.memory.MemoryIntegrationTestSupport;
import org.bpmscript.integration.internal.memory.MemoryMessageBus;
import org.bpmscript.loanbroker.LoanRequest;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;

/**
 * Have one script call another, using in memory bus and engine
 */
public class JavaLoanBrokerTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	public void testChannel() throws Exception {

		MemoryIntegrationTestSupport integrationTestSupport = new MemoryIntegrationTestSupport();
		integrationTestSupport.execute(new ITestCallback<IServiceLookup>() {
        
            public void execute(final IServiceLookup services) throws Exception {
                final int total = 100;
                
                final MemoryMessageBus bus = services.get("bus");
                final BpmScriptEngine engine = services.get("engine");
                
                IVersionedDefinitionManager processManager = engine.getVersionedDefinitionManager();
                processManager.createDefinition("id", new JavaProcessDefinition("loanBroker", LoanBrokerProcess.class.getName()));
                
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                    public void execute(int count) throws Exception {
                        InvocationMessage message = new InvocationMessage();
                        message.setArgs(null, "loanBroker", "requestBestRate", new Object[] { new LoanRequest("asdf", 1, 1000) });
                        message.setReplyTo("recorder");
                        bus.send("bpmscript-first", message);
                        log.debug("sent message");
                    }
                }, new IWaitForCallback() {
                    public void call() throws Exception {
                        RecordingAdapter recorder = services.get("recording");
                        BlockingQueue<IInternalMessage> internalMessages = recorder.getMessages();
                        for(int i = 0; i < total; i++) {
                            Object result = internalMessages.poll(total, TimeUnit.SECONDS);
                            assertNotNull(result);
                            if (result instanceof ExceptionMessage) {
                                ExceptionMessage exceptionMessage = (ExceptionMessage) result;
                                log.error(exceptionMessage.getThrowable(), exceptionMessage.getThrowable());
                                fail(exceptionMessage.getThrowable().getMessage());
                            }
                        }
                    }
                }, false));
            }
        
        });

	}

}
