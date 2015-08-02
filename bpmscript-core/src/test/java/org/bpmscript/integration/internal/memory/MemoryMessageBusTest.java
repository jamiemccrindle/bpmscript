package org.bpmscript.integration.internal.memory;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.memory.MemoryAddressRegistry;
import org.bpmscript.integration.internal.memory.MemoryMessageBus;

import junit.framework.TestCase;

public class MemoryMessageBusTest extends TestCase {
    
    public final class QueuingMessageReceiver implements IMessageReceiver {

        private BlockingQueue<IInternalMessage> queue = new LinkedBlockingQueue<IInternalMessage>();
        public void onMessage(IInternalMessage invocationMessage) {
            queue.add(invocationMessage);
        }
        public BlockingQueue<IInternalMessage> getQueue() {
            return queue;
        }

    }

    public void testMessageBus() throws Exception {
        MemoryAddressRegistry registry = new MemoryAddressRegistry();
        HashMap<String, IMessageReceiver> endpoints = new HashMap<String, IMessageReceiver>();
        QueuingMessageReceiver receiver = new QueuingMessageReceiver();
        endpoints.put("test", receiver);
        registry.setEndpoints(endpoints);
        MemoryMessageBus bus = new MemoryMessageBus();
        bus.setAddressRegistry(registry);
        bus.afterPropertiesSet();
        InvocationMessage message = new InvocationMessage();
        message.setArgs("test");
        bus.send("test", message);
        IInternalMessage poll = receiver.getQueue().poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(poll);
        assertTrue(poll instanceof IInvocationMessage);
        assertEquals("test", ((IInvocationMessage) poll).getArgs()[0]);
    }
}
