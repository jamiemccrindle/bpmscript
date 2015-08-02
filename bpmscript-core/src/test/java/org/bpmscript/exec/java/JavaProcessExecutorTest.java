package org.bpmscript.exec.java;

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
import org.bpmscript.exec.java.seriaize.JavaSerializingContinuationService;
import org.bpmscript.journal.memory.MemoryContinuationJournal;
import org.springframework.util.StopWatch;

public class JavaProcessExecutorTest extends TestCase {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	public JavaProcessExecutorTest() {
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testProcessExecutor() throws Throwable {
		final JavaProcessExecutor executor = new JavaProcessExecutor();
		JavaSerializingContinuationService continuationService = new JavaSerializingContinuationService();
		MemoryContinuationJournal continuationJournal = new MemoryContinuationJournal();
		continuationService.setContinuationJournal(continuationJournal);
		executor.setContinuationService(continuationService);
        final AtomicReference<HashMap<String, String>> aqueueId = new AtomicReference<HashMap<String,String>>(new HashMap<String, String>());
        TestScriptChannel testScriptChannel = new TestScriptChannel() {
            @Override
            public void send(String fromPid, String fromBranch, String fromVersion, String queueId,
                    Object message) throws BpmScriptException {
                aqueueId.get().put(fromPid, queueId);
            }
        };
        executor.setChannel(testScriptChannel);

		JavaProcessDefinition scriptDefinition = new JavaProcessDefinition(
				"test", TestProcess.class.getName());

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
                log.debug("sending first");
				IExecutorResult result = executor.sendFirst(processId, pid, branch, pausedVersion,
						scriptDefinition, message, operation,
						parentVersion);
				log.debug("sent first");

				throwIfFailed(result);
				assertTrue(result instanceof PausedResult);
			}

			{
		        String version = continuationJournal.createVersion(pid, branch);
                log.debug("sending next");
				IExecutorResult result = executor.sendNext(processId, "test", pid, branch, version, aqueueId.get().get(pid), message);
				log.debug("sent next");

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
