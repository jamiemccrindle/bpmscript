package org.bpmscript.endtoend;

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
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.Recorder;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple end to end test that loops through and calls Recorder a number of times
 */
public class SimpleTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public void testChannel() throws Exception {
        final int total = 3;
        final int loopcount = 2;
        final CountDownLatch latch = new CountDownLatch(total);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/endtoend/spring.xml");

        try {

            final IMessageSender bus = (IMessageSender) context.getBean("bus");
            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");

            processManager.createDefinition("id", new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/endtoend/spring.js")));
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
                public void execute(int count) throws Exception {
                    InvocationMessage message = new InvocationMessage();
                    message.setArgs(null, "test", "test", new Object[] { "testcontent", loopcount });
                    message.setReplyTo("recording");
                    bus.send("bpmscript-first", message);
                }
            }, new IWaitForCallback() {
                public void call() throws Exception {
                    if (!latch.await(total * loopcount * 2, TimeUnit.SECONDS)) {
                        fail("timed out waiting for results");
                    }
//                    latch.await();
                }
            }, false));

            Recorder recorder = (Recorder) context.getBean("recorder");
            BlockingQueue<Object> messages = recorder.getMessages();
            assertEquals(total * loopcount, messages.size());

        } finally {
            context.destroy();
        }

        log.info("done");

    }

    public static void main(String... args) throws Exception {
        SimpleTest test = new SimpleTest();
        test.testChannel();
    }

}
