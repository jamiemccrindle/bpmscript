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
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.util.StreamService;

/**
 * Have one script call another, using in memory bus and engine
 */
public class MemoryReplyTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	public void testChannel() throws Exception {

		MemoryIntegrationTestSupport integrationTestSupport = new MemoryIntegrationTestSupport();
		integrationTestSupport.execute(new ITestCallback<IServiceLookup>() {
        
            public void execute(IServiceLookup services) throws Exception {
                int total = 1;
                final int loopcount = 2;
                final CountDownLatch latch = new CountDownLatch(loopcount * total);
                
                final MemoryMessageBus bus = services.get("bus");
                final BpmScriptEngine engine = services.get("engine");
                
                IVersionedDefinitionManager processManager = engine.getVersionedDefinitionManager();
                processManager.createDefinition("id", new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/integration/memory/reply.js")));
                engine.setInstanceListener(new LoggingInstanceListener() {
                    @Override
                    public void instanceCompleted(String pid, ICompletedResult result) {
                        log.debug("completed " + pid);
                        latch.countDown();
                    }
                    @Override
                    public void instanceFailed(String pid, IFailedResult result) {
                        Throwable throwable = result.getThrowable();
                        log.error(throwable, throwable);
                        fail(throwable.getMessage());
                    }
                });
                
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                    public void execute(int count) throws Exception {
                        InvocationMessage message = new InvocationMessage();
                        message.setArgs(null, "test", "test", new Object[] {new Integer(loopcount)});
                        message.setReplyTo("recorder");
                        bus.send("bpmscript-first", message);
                    }
                }, new IWaitForCallback() {
                    public void call() throws Exception {
                        latch.await();
                    }
                }, false));
            }
        
        });

	}

}
