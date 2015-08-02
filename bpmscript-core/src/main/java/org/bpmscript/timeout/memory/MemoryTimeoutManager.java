/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bpmscript.timeout.memory;

import java.util.Timer;
import java.util.TimerTask;

import org.bpmscript.timeout.ITimeoutListener;
import org.bpmscript.timeout.ITimeoutManager;

/**
 * An in memory timeout manager
 */
public class MemoryTimeoutManager implements ITimeoutManager {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
	private class TimeoutTask extends TimerTask {

		private Object data;
		
		public TimeoutTask(Object data) {
			this.data = data;
		}
		
		@Override
		public void run() {
			listener.timedOut(data);
		}
		
	}
	
	private Timer timer = new Timer();
	
	private ITimeoutListener listener = null;
	
	public MemoryTimeoutManager() {
        super();
    }

    public MemoryTimeoutManager(ITimeoutListener listener) {
        super();
        this.listener = listener;
    }

    public void addTimeout(Object data, long delay) {
        try {
            timer.schedule(new TimeoutTask(data), delay);
        } catch (IllegalStateException e ) {
            log.warn(e);
        }
	}

	public void setTimeoutListener(ITimeoutListener listener) {
		this.listener = listener;
	}

    /**
     * @see org.bpmscript.ILifeCycle#start()
     */
    public void start() throws Exception {
    }

    /**
     * @see org.bpmscript.ILifeCycle#stop()
     */
    public void stop() throws Exception {
        timer.cancel();
    }

}
