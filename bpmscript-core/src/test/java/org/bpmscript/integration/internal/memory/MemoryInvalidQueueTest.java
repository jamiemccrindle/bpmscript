package org.bpmscript.integration.internal.memory;

import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ICorrelationId;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMutableAddressRegistry;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.ResponseMessage;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MemoryInvalidQueueTest extends TestCase {

    public void testChannel() throws Exception {
        int total = 1;
        final CountDownLatch latch = new CountDownLatch(total);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/integration/memory/spring.xml");

        try {

            final MemoryMessageBus bus = (MemoryMessageBus) context.getBean("bus");
            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");
            final IMutableAddressRegistry mutableAddressRegistry = (IMutableAddressRegistry) context
                    .getBean("mutableAddressRegistry");

            mutableAddressRegistry.register("badaddress", new IMessageReceiver() {

                public void onMessage(IInternalMessage internalMessage) {
                    CorrelationIdFormatter formatter = CorrelationIdFormatter.DEFAULT_INSTANCE;
                    ICorrelationId id = formatter.parse(internalMessage.getCorrelationId());
                    IInvocationMessage invocationMessage = ((IInvocationMessage) internalMessage);
                    bus.send(invocationMessage.getReplyTo(), new ResponseMessage(formatter.format(id.getPid(), id
                            .getBranch(), id.getVersion(), "JustWrong"), "DoesntMatter"));
                }

            });

            processManager.createDefinition("id", new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE
                            .getResourceAsString("/org/bpmscript/integration/memory/invalidqueue.js")));

            engine.setInstanceListener(new LoggingInstanceListener() {
                @Override
                public void instanceCompleted(String pid, ICompletedResult result) {
                    super.instanceCompleted(pid, result);
                    fail("should not complete");
                }

                @Override
                public void instanceFailed(String pid, IFailedResult result) {
                    latch.countDown();
                }
            });

            IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                public void execute(int count) throws Exception {
                    InvocationMessage message = new InvocationMessage();
                    message.setArgs(null, "test", "test", new Object[] { "testcontent" });
                    message.setReplyTo("recorder");
                    bus.send("bpmscript-first", message);
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
