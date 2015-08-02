package org.bpmscript.integration.internal.memory;

import java.util.HashMap;

import junit.framework.TestCase;

import org.bpmscript.InvalidQueueException;
import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IBenchmarkResults;
import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ICorrelationId;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.process.IBpmScriptFacade;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.timeout.TimeCalculator;
import org.bpmscript.util.StreamService;
import org.bpmscript.web.SerializableHttpServletRequest;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MemoryConversationTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    public void testChannel() throws Throwable {
        
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/org/bpmscript/integration/memory/spring.xml");
        
        final IVersionedDefinitionManager processManager = (IVersionedDefinitionManager) context.getBean("versionedDefinitionManager");
        final IConversationCorrelator conversationCorrelator = (IConversationCorrelator) context.getBean("conversationCorrelator");
        final IBpmScriptFacade bpmScriptFacade = (IBpmScriptFacade) context.getBean("bpmScriptFacade");
        final IContinuationJournal continuationJournal = (IContinuationJournal) context.getBean("continuationJournal");
        
        processManager.createDefinition("id", new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/integration/memory/memoryconversation.js")));
        
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("test", new String[] {"test"});
        final SerializableHttpServletRequest request = new SerializableHttpServletRequest(params);
        
        final int total = 1;
        
        Benchmark benchmark = new Benchmark();
        IBenchmarkResults results = benchmark.execute(total, new IBenchmarkCallback() {
        
            public void execute(int count) throws Exception {
                long pauseTime = TimeCalculator.DEFAULT_INSTANCE.minutes(1 * total);
                Object result = bpmScriptFacade.call("test", "test", pauseTime, request);
                assertTrue(result instanceof IInvocationMessage);
                if (result instanceof IInvocationMessage) {
                    IInvocationMessage firstResult = (IInvocationMessage) result;
                    assertNotNull(firstResult.getCorrelationId());
                    IInvocationMessage secondResult = (IInvocationMessage) conversationCorrelator.call(
                            firstResult.getCorrelationId(), pauseTime, request);
                    assertNotNull(secondResult);
                    try {
                        IInvocationMessage thirdResult = (IInvocationMessage) conversationCorrelator.call(
                                firstResult.getCorrelationId(), pauseTime, request);
                        assertNotNull(thirdResult);
                    } catch(InvalidQueueException e) {
                        log.info("first bang");
                        CorrelationIdFormatter formatter = CorrelationIdFormatter.DEFAULT_INSTANCE;
                        ICorrelationId parse = formatter.parse(firstResult.getCorrelationId());
                        log.info("creating branch");
                        String newBranch = continuationJournal.createBranch(parse.getVersion());
                        log.info("sending again with new branch " + newBranch);
                        IInvocationMessage thirdResult = (IInvocationMessage) conversationCorrelator.call(
                                formatter.format(parse.getPid(), newBranch, parse.getVersion(), parse.getQueueId()), pauseTime, request);
                        assertNotNull(thirdResult);
                    }
                }
            }
        
        }, null, true);
        IBenchmarkPrinter.STDOUT.print(results);
        
    }
}
