package org.bpmscript.integration.internal.memory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.NoopInstanceListener;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MemoryTimeoutTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	public void testChannel() throws Exception {
		
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/org/bpmscript/integration/memory/timeout.xml");
		MemoryIntegrationTestSupport integrationTestSupport = new MemoryIntegrationTestSupport(context);
		integrationTestSupport.execute(new ITestCallback<IServiceLookup>() {
        
            public void execute(IServiceLookup services) throws Exception {
                final int total = 1;
                final CountDownLatch latch = new CountDownLatch(total);
                final MemoryMessageBus bus = services.get("bus");
                final BpmScriptEngine engine = services.get("engine");
                
                IVersionedDefinitionManager processManager = engine.getVersionedDefinitionManager();
                processManager.createDefinition("id", new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/integration/memory/timeout.js")));
                engine.setInstanceListener(new NoopInstanceListener() {
                
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
                    
                    @Override
                    public void instancePaused(String pid, IPausedResult result) {
                        log.debug("paused " + pid);
                    }
                    
                    @Override
                    public void instanceCreated(String pid, IDefinition definition) {
                        log.debug("created " + pid + " with name " + definition.getName());
                    }
                
                });
                
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback(){
                    public void execute(int count) throws Exception {
                        InvocationMessage message = new InvocationMessage();
                        message.setArgs(null, "test", "test", new Object[] {"hello"});
                        message.setReplyTo("recorder");
                        bus.send("bpmscript-first", message);
                    }
                }, new IWaitForCallback() {
                    public void call() throws Exception {
                        latch.await(5 + total * 1000, TimeUnit.SECONDS);
                    }
                }, false));
            }
        
        });

	}

}
