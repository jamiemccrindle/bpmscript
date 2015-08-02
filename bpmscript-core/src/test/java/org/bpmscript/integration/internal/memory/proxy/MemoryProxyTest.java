package org.bpmscript.integration.internal.memory.proxy;

import junit.framework.TestCase;

import org.bpmscript.IFailedResult;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.channel.ISyncChannel;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.memory.MemoryIntegrationTestSupport;
import org.bpmscript.integration.internal.memory.MemoryMessageBus;
import org.bpmscript.integration.internal.proxy.BpmScriptProxyFactoryBean;
import org.bpmscript.integration.internal.proxy.BpmScriptFacade;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.listener.NoopInstanceListener;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.util.StreamService;

public class MemoryProxyTest extends TestCase {
	
	public static interface ISomething {
		public String doSomething(String one, Integer two);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testProxy() throws Exception {

        MemoryIntegrationTestSupport memoryIntegrationTestSupport = new MemoryIntegrationTestSupport();
        memoryIntegrationTestSupport.execute(new ITestCallback<IServiceLookup>() {
        
            public void execute(IServiceLookup services) throws Exception {

                int total = 100;

                final MemoryMessageBus bus = services.get("bus");
                final BpmScriptEngine engine = services.get("engine");
                
                IVersionedDefinitionManager processManager = engine.getVersionedDefinitionManager();
                processManager.createDefinition("id", new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/integration/memory/proxy.js")));

                engine.setInstanceListener(new NoopInstanceListener() {
                
                    @Override
                    public void instanceFailed(String pid, IFailedResult result) {
                        result.getThrowable().printStackTrace();
                        fail(pid + " failed");
                    }
                
                });

                BpmScriptFacade bpmScriptFacade = new BpmScriptFacade();
                bpmScriptFacade.setAddress("bpmscript-first");
                bpmScriptFacade.setReplyTo("sync");
                bpmScriptFacade.setSender(bus);
                bpmScriptFacade.setSyncChannel((ISyncChannel) services.get("sync"));
                
                BpmScriptProxyFactoryBean proxyFactoryBean = new BpmScriptProxyFactoryBean();
                proxyFactoryBean.setDefaultTimeout(10000);
                proxyFactoryBean.setDefinitionName("test");
                proxyFactoryBean.setBpmScriptFacade(bpmScriptFacade);
                proxyFactoryBean.setInterfaceClass(ISomething.class);
                final ISomething something = (ISomething) proxyFactoryBean.getObject();
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                    public void execute(int count) throws Exception {
                        String result = something.doSomething("one", 2);
                        assertEquals("Hello World! one 2", result);
                    }
                }, null, true));
            }
        
        });
        
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
