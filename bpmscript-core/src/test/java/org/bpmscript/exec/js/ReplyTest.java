package org.bpmscript.exec.js;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

public class ReplyTest extends TestCase {

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

		final JavascriptProcessDefinition scriptDefinition = new JavascriptProcessDefinition(
				"test", StreamService.DEFAULT_INSTANCE
						.readFully(getClass().getResourceAsStream(
								"/org/bpmscript/exec/main.js")));

		final Object message = "";
		final String processId = "1234";
		final String operation = "test";
        final String parentVersion = null;
        final String pid = "12345";
        final String branch = continuationJournal.createMainBranch(pid);
        final String pausedVersion = continuationJournal.createVersion(pid, branch);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> aqueueId = new AtomicReference<String>(null);

		final TestScriptChannel testScriptChannel = new TestScriptChannel() {
		    
		    @Override
		    public void send(String fromPid, String fromBranch, String fromVersion, String queueId, Object message)
		            throws BpmScriptException {
                aqueueId.set(queueId);
		    }
		    
		};
		executor.setChannel(testScriptChannel);

		{
			IExecutorResult result;
			try {
				result = executor.sendFirst(processId, pid, branch, pausedVersion, scriptDefinition,
						message, operation, parentVersion);
				throwIfFailed(result);
				assertTrue(result instanceof PausedResult);
			} catch (BpmScriptException e) {
				e.printStackTrace();
				fail(e.getMessage());
			} catch (Throwable e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
			public void run() {
				try {
					Object reply = testScriptChannel.getReply(pid, 10000);
					System.out.println(reply);
				} catch (InterruptedException e) {
					e.printStackTrace();
					fail(e.getMessage());
				} finally {
					latch.countDown();
				}
			}
		});
		{
		}

		{
			String version = continuationJournal.createVersion(pid, branch);
			IExecutorResult result = executor.sendNext(processId, "test", pid, branch, version, aqueueId.get(),
					message);

			throwIfFailed(result);
			assertTrue(result instanceof CompletedResult);
		}
		
		latch.await();

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
