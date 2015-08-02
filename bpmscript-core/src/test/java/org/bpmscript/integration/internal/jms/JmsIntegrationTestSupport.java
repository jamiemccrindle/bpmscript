package org.bpmscript.integration.internal.jms;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.UUID;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.MemoryEngineTestSupport;
import org.bpmscript.integration.internal.adapter.BpmScriptFirstAdapter;
import org.bpmscript.integration.internal.adapter.BpmScriptNextAdapter;
import org.bpmscript.integration.internal.adapter.LoggingAdapter;
import org.bpmscript.integration.internal.adapter.RecordingAdapter;
import org.bpmscript.integration.internal.adapter.SpringAdapter;
import org.bpmscript.integration.internal.adapter.TimeoutAdapter;
import org.bpmscript.integration.internal.memory.MemoryAddressRegistry;
import org.bpmscript.integration.internal.memory.MemorySyncChannel;
import org.bpmscript.process.IBpmScriptEngine;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Test support that creates an internal JMS Message Bus
 */
public class JmsIntegrationTestSupport implements ITestSupport<IServiceLookup> {

    private ApplicationContext context;
    
    /**
     * @see org.bpmscript.test.ITestSupport#execute(org.bpmscript.test.ITestCallback)
     */
    public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
        String syncQueueName = "bpmscript-" + InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID().toString();
        SyncChannelFormatter syncChannelFormatter = new SyncChannelFormatter(syncQueueName);

        final JmsMessageBus bus = new JmsMessageBus();
        bus.setSyncFormatter(syncChannelFormatter);
        final MemoryAddressRegistry registry = new MemoryAddressRegistry();
        bus.setAddressRegistry(registry);

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://broker1?marshal=false&broker.persistent=false");
        final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory);
        ActiveMQQueue queue = new ActiveMQQueue("bpmscript");
        ActiveMQQueue syncQueue = new ActiveMQQueue(syncQueueName);
        
        SerializingMessageConverter messageConverter = new SerializingMessageConverter();

        JmsTemplate template = new JmsTemplate(pooledConnectionFactory);
        template.setDefaultDestination(queue);
        template.setMessageConverter(messageConverter);
        template.afterPropertiesSet();
        
        JmsTemplate syncTemplate = new JmsTemplate(pooledConnectionFactory);
        syncTemplate.setDefaultDestination(syncQueue);
        syncTemplate.setMessageConverter(messageConverter);
        syncTemplate.afterPropertiesSet();
        
        bus.setTemplate(template);
        
        final DefaultMessageListenerContainer syncContainer = new DefaultMessageListenerContainer();
        syncContainer.setConnectionFactory(connectionFactory);
        syncContainer.setDestination(syncQueue);
        syncContainer.setMaxConcurrentConsumers(3);
        syncContainer.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        syncContainer.setMessageListener(bus);
        syncContainer.afterPropertiesSet();
        
        final DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(queue);
        container.setMaxConcurrentConsumers(3);
        container.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
        container.setMessageListener(bus);
        container.afterPropertiesSet();

        MemoryEngineTestSupport engineTestSupport = new MemoryEngineTestSupport(bus);
        engineTestSupport.execute(new ITestCallback<IServiceLookup>() {
        
            public void execute(IServiceLookup services) throws Exception {

                HashMap<String, IMessageReceiver> endpoints = new HashMap<String, IMessageReceiver>();
                BpmScriptFirstAdapter bpmScriptFirstAdapter = new BpmScriptFirstAdapter((IBpmScriptEngine) services.get("engine"), bus);
                BpmScriptNextAdapter bpmScriptNextAdapter = new BpmScriptNextAdapter((IBpmScriptEngine) services.get("engine"), bus, "error");

                final TimeoutAdapter timeoutAdapter = new TimeoutAdapter(bus);
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
                
                container.start();
                syncContainer.start();
                
                ServiceLookup serviceLookup = new ServiceLookup(services);
                serviceLookup.addService("bus", bus);
                serviceLookup.addService("recording", recordingAdapter);
                serviceLookup.addService("sync", syncChannel);
                
                try {
                    callback.execute(serviceLookup);
                } finally {
                    timeoutAdapter.destroy();
                    pooledConnectionFactory.stop();
                    container.stop();
                    container.destroy();
                    syncContainer.stop();
                    syncContainer.destroy();
                }
            }
        
        });
    }

}
