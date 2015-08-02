package org.bpmscript.integration.internal.memory;

import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.bpmscript.IKilledResult;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.paging.Query;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IAutoStartManager;
import org.bpmscript.process.IInstanceKiller;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.IInstanceStatusManager;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.LoggingInstanceListener;
import org.bpmscript.util.StreamService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MemoryAutoStartTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public void testChannel() throws Exception {

        final CountDownLatch killLatch = new CountDownLatch(2);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "/org/bpmscript/integration/memory/spring.xml");

        try {

            final BpmScriptEngine engine = (BpmScriptEngine) context.getBean("engine");
            final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context
                    .getBean("versionedDefinitionManager");
            final IInstanceManager instanceManager = (IInstanceManager) context.getBean("instanceManager");
            final IInstanceStatusManager instanceStatusManager = (IInstanceStatusManager) context
                    .getBean("instanceStatusManager");
            final IAutoStartManager autoStartManager = (IAutoStartManager) context.getBean("autoStartManager");
            final IInstanceKiller instanceKiller = (IInstanceKiller) context.getBean("instanceKiller");

            engine.setInstanceListener(new LoggingInstanceListener() {
                @Override
                public void instanceKilled(String pid, IKilledResult result) {
                    log.debug("instance is dying");
                    killLatch.countDown();
                }
            });

            String did1 = "did1";
            processManager.createDefinition(did1, new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE
                            .getResourceAsString("/org/bpmscript/integration/memory/autostart/autostart1.js")));
            autoStartManager.startup("test", true);
            assertEquals(1, instanceManager.getInstancesForDefinition(Query.ALL, did1).getResults().size());
            Thread.sleep(500);
            assertEquals(1, instanceStatusManager.getLiveInstances(did1).size());

            autoStartManager.startup("test", true);
            Thread.sleep(500);
            assertEquals(1, instanceStatusManager.getLiveInstances(did1).size());

            String did2 = "did2";
            processManager.createDefinition(did2, new JavascriptProcessDefinition("test",
                    StreamService.DEFAULT_INSTANCE
                            .getResourceAsString("/org/bpmscript/integration/memory/autostart/autostart2.js")));
            processManager.setDefinitionAsPrimary(did2);
            autoStartManager.startup("test", true);
            Thread.sleep(500);
            assertEquals(0, instanceStatusManager.getLiveInstances(did1).size());
            assertEquals(1, instanceStatusManager.getLiveInstances(did2).size());

            autoStartManager.startup("test", true);
            Thread.sleep(500);
            assertEquals(1, instanceStatusManager.getLiveInstances(did2).size());

            int numberKilled = instanceKiller.killDefinitionInstances(did2, "Die die die");

            assertEquals(1, numberKilled);

            killLatch.await();
        } finally {
            context.destroy();
        }

    }

}
