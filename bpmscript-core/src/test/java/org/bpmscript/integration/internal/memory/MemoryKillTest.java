package org.bpmscript.integration.internal.memory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MemoryKillTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public void testChannel() throws Exception {
        final int total = 1;
        final CountDownLatch pauseLatch = new CountDownLatch(total);
        final CountDownLatch killLatch = new CountDownLatch(total);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/integration/memory/spring.xml");

        try {

            final MemoryMessageBus bus = (MemoryMessageBus) context.getBean("bus");
            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");
            final IContinuationJournal continuationJournal = (IContinuationJournal) context
                    .getBean("continuationJournal");
            final IInstanceManager instanceManager = (IInstanceManager) context.getBean("instanceManager");

            final String definitionId = "id";
            processManager.createDefinition(definitionId, new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/integration/memory/killme.js")));
            engine.setInstanceListener(new LoggingInstanceListener() {
                @Override
                public void instanceKilled(String pid, IKilledResult result) {
                    killLatch.countDown();
                }

                @Override
                public void instancePaused(String pid, IPausedResult result) {
                    super.instancePaused(pid, result);
                    pauseLatch.countDown();
                }
            });

            IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                public void execute(int count) throws Exception {
                    InvocationMessage message = new InvocationMessage();
                    message.setArgs(null, "test", "test", new Object[] { "Hello" });
                    message.setReplyTo("recorder");
                    bus.send("bpmscript-first", message);
                    pauseLatch.await();
                    log.info("attempting the kill");
                    IPagedResult<IInstance> instances = instanceManager.getInstancesForDefinition(Query.ALL,
                            definitionId);
                    List<IInstance> results = instances.getResults();
                    assertTrue(results.size() > 0);
                    for (IInstance instance : results) {
                        Collection<String> branches = continuationJournal.getBranchesForPid(instance.getId());
                        assertTrue(branches.size() > 0);
                        for (String branch : branches) {
                            engine.kill(instance.getId(), branch, "Die die die");
                        }
                    }
                }
            }, new IWaitForCallback() {

                public void call() throws Exception {
                    killLatch.await(total * 1, TimeUnit.SECONDS);
                }

            }, false));

        } finally {
            context.destroy();
        }

    }

}
