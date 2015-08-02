package org.bpmscript.integration.internal.memory;

import java.util.HashMap;

import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.MemoryEngineTestSupport;
import org.bpmscript.integration.internal.adapter.BpmScriptFirstAdapter;
import org.bpmscript.integration.internal.adapter.BpmScriptNextAdapter;
import org.bpmscript.integration.internal.adapter.LoggingAdapter;
import org.bpmscript.integration.internal.adapter.RecordingAdapter;
import org.bpmscript.integration.internal.adapter.SpringAdapter;
import org.bpmscript.integration.internal.adapter.TimeoutAdapter;
import org.bpmscript.process.IBpmScriptEngine;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.springframework.context.ApplicationContext;

public class MemoryIntegrationTestSupport implements ITestSupport<IServiceLookup>{
    
    private ApplicationContext context;
    
    public MemoryIntegrationTestSupport(ApplicationContext context) {
        super();
        this.context = context;
    }
    
    public MemoryIntegrationTestSupport() {}

    /**
     * @see org.bpmscript.test.ITestSupport#execute(org.bpmscript.test.ITestCallback)
     */
    public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
        final MemoryMessageBus bus = new MemoryMessageBus();
        final MemoryAddressRegistry registry = new MemoryAddressRegistry();
        bus.setAddressRegistry(registry);
        bus.afterPropertiesSet();

        try {
            MemoryEngineTestSupport engineFactory = new MemoryEngineTestSupport(bus);
            engineFactory.execute(new ITestCallback<IServiceLookup>() {
            
                public void execute(IServiceLookup services) throws Exception {
                    IBpmScriptEngine engine = services.get("engine");
                    HashMap<String, IMessageReceiver> endpoints = new HashMap<String, IMessageReceiver>();
                    BpmScriptFirstAdapter bpmScriptFirstAdapter = new BpmScriptFirstAdapter(engine, bus);
                    BpmScriptNextAdapter bpmScriptNextAdapter = new BpmScriptNextAdapter(engine, bus, "error");
    
                    TimeoutAdapter timeoutAdapter = new TimeoutAdapter(bus);
                    final RecordingAdapter recordingAdapter = new RecordingAdapter();
                    final MemorySyncChannel syncChannel = new MemorySyncChannel();
                    SpringAdapter springAdapter = new SpringAdapter(context, bus);
                    endpoints.put("spring", springAdapter);
                    endpoints.put("bpmscript-first", bpmScriptFirstAdapter);
                    endpoints.put("bpmscript-next", bpmScriptNextAdapter);
                    endpoints.put("timeout", timeoutAdapter);
                    endpoints.put("recorder", recordingAdapter);
                    endpoints.put("error", new LoggingAdapter());
                    endpoints.put("sync", syncChannel);
                    registry.setEndpoints(endpoints);
                    
                    ServiceLookup serviceLookup = new ServiceLookup(services);
                    serviceLookup.addService("bus", bus);
                    serviceLookup.addService("recording", recordingAdapter);
                    serviceLookup.addService("sync", syncChannel);
                    
                    callback.execute(serviceLookup);
                }
            
            });
        } finally {
            bus.destroy();
        }

    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

}
