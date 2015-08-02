package org.bpmscript.exec.js;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.exec.CompletedResult;
import org.bpmscript.exec.FailedResult;
import org.bpmscript.exec.PausedResult;
import org.bpmscript.exec.TestScriptChannel;
import org.bpmscript.exec.js.scope.ScopeService;
import org.bpmscript.exec.js.serialize.JavascriptSerializingContinuationService;
import org.bpmscript.journal.memory.MemoryContinuationJournal;
import org.bpmscript.util.StreamService;
import org.springframework.util.StopWatch;

public class JavaScriptProcessExecutorTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	public JavaScriptProcessExecutorTest() {
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testProcessExecutor() throws Throwable {
		final JavascriptProcessExecutor executor = new JavascriptProcessExecutor();
		JavascriptSerializingContinuationService continuationService = new JavascriptSerializingContinuationService();
		MemoryContinuationJournal continuationJournal = new MemoryContinuationJournal();
		continuationService.setContinuationStore(continuationJournal);
		executor.setContinuationService(continuationService);
		ScopeService scopeService = new ScopeService();
		executor.setScopeService(scopeService);
		JavascriptExecutor javascriptExecutor = new JavascriptExecutor();
		executor.setScriptExecutor(javascriptExecutor);
        final AtomicReference<HashMap<String, String>> aqueueId = new AtomicReference<HashMap<String,String>>(new HashMap<String, String>());
        TestScriptChannel testScriptChannel = new TestScriptChannel() {
            @Override
            public void send(String fromPid, String fromBranch, String fromVersion, String queueId,
                    Object message) throws BpmScriptException {
                aqueueId.get().put(fromPid, queueId);
            }
        };
        executor.setChannel(testScriptChannel);

        JavascriptProcessDefinition scriptDefinition = new JavascriptProcessDefinition(
				"test", StreamService.DEFAULT_INSTANCE
						.readFully(getClass().getResourceAsStream(
								"/org/bpmscript/exec/main.js")));

		Object message = "";
		String processId = "1234";
		String operation = "test";
        String parentVersion = null;

		int total = 1;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("test");
		for (int i = 0; i < total; i++) {
		    
		    
            String pid = UUID.randomUUID().toString();
            String branch = continuationJournal.createMainBranch(pid);
            String pausedVersion = continuationJournal.createVersion(pid, branch);

			{
                log.info("sending first");
				IExecutorResult result = executor.sendFirst(processId, pid, branch, pausedVersion,
						scriptDefinition, message, operation,
						parentVersion);
				log.info("sent first");

				throwIfFailed(result);
				assertTrue(result instanceof PausedResult);
			}

			{
		        String version = continuationJournal.createVersion(pid, branch);
                log.info("sending next");
				IExecutorResult result = executor.sendNext(processId, "test", pid, branch, version, aqueueId.get().get(pid),
						 message);
				log.info("sent next");

				throwIfFailed(result);
				assertTrue(result instanceof CompletedResult);
			}
		}
		stopWatch.stop();
		System.out.println("total: " + stopWatch.getTotalTimeMillis());
		double average = (stopWatch.getTotalTimeMillis() / (double) total);
		System.out.println("average: " + average);
		System.out.println("tpm: " + (60000.0 / average));
		// System.out.println("bytes: " + continuationJournal.getContinuationVersion(pausedVersion).length);

	}

	private void throwIfFailed(IExecutorResult result) throws Throwable {
		assertNotNull(result);
		if (result instanceof FailedResult) {
			IFailedResult failedResult = (IFailedResult) result;
			throw failedResult.getThrowable();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
