package org.bpmscript.integration.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Recorder {
	
	private BlockingQueue<Object> messages = new LinkedBlockingQueue<Object>();
	
	public String record(String message) {
		messages.add(message);
		return message;
	}

	public BlockingQueue<Object> getMessages() {
		return messages;
	}

}
