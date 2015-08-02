package org.bpmscript.integration.internal.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.correlation.ICorrelationService;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IInstanceListener;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.test.ITestCallback;
import org.springframework.context.ApplicationContext;

public class MemoryCorrelateTest extends TestCase {

    public void testChannel() throws Exception {
        BpmScriptTestSupport testSupport = new BpmScriptTestSupport(1, 
                "/org/bpmscript/integration/memory/spring.xml",
                "/org/bpmscript/integration/memory/correlate.js");
        testSupport.execute(new ITestCallback<ApplicationContext>() {
            public void execute(ApplicationContext services) throws Exception {
                MemoryMessageBus bus = (MemoryMessageBus) services.getBean("bus");
                BpmScriptEngine engine = (BpmScriptEngine) services.getBean("engine");
                final IInstanceListener instanceListener = engine.getInstanceListener();
                final CountDownLatch pausedLatch = new CountDownLatch(1);
                engine.setInstanceListener(new LoggingInstanceListener() {
                    @Override
                    public void instancePaused(String pid, IPausedResult result) {
                        super.instancePaused(pid, result);
                        pausedLatch.countDown();
                    }
                    @Override
                    public void instanceCompleted(String pid, ICompletedResult result) {
                        instanceListener.instanceCompleted(pid, result);
                    }
                });
                InvocationMessage message = new InvocationMessage();
                message.setArgs(null, "test", "test", new Object[] { "Hello" });
                message.setReplyTo("recorder");
                bus.send("bpmscript-first", message);
                assertTrue(pausedLatch.await(5, TimeUnit.SECONDS));
                ICorrelationService correlationService = (ICorrelationService) services.getBean("correlationService");
                correlationService.send("channel", "Hello World!");
            }
        });
    }

}
