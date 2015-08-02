package org.bpmscript.timeout.memory;

import java.util.concurrent.CountDownLatch;

import org.bpmscript.timeout.ITimeoutListener;

import junit.framework.TestCase;

public class MemoryTimeoutManagerTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testTimeouts() throws Exception {
		final int total = 10;
		final CountDownLatch latch = new CountDownLatch(total);
		MemoryTimeoutManager timeoutManager = new MemoryTimeoutManager();
		timeoutManager.setTimeoutListener(new ITimeoutListener() {
		
			public void timedOut(Object data) {
				assertEquals(total - latch.getCount(), ((Integer) data).intValue());				latch.countDown();
			}
		
		});
		
		for(int i = 0; i < total; i++) {
			timeoutManager.addTimeout(i, 10);
			Thread.sleep(15);
		}
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
