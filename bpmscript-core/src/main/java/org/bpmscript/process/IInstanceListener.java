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
package org.bpmscript.process;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;

/**
 * Observer interface for listening for instance state changes
 */
public interface IInstanceListener {
	
    /**
     * Called when an instance is first created
     * 
     * @param pid the instance that was created
     * @param definition the definition that created it
     */
	void instanceCreated(String pid, IDefinition definition);
	
	/**
	 * Called when an instance pauses i.e. persists itself and is
	 * not yet completed.
	 * 
	 * @param pid the instance that pauses
	 * @param result the paused result, including the continuation
	 */
	void instancePaused(String pid, IPausedResult result);
	
	/**
	 * Called if the instance fails
	 * 
	 * @param pid the failing instance
	 * @param result the failed result including the exception
	 */
    void instanceFailed(String pid, IFailedResult result);
    
    /**
     * Called when an instance is killed
     * 
     * @param pid the instance id that was killed
     * @param result the killed result
     */
    void instanceKilled(String pid, IKilledResult result);
    
    /**
     * Called when an instance completes
     * 
     * @param pid the instance that completed
     * @param result the completed result
     */
	void instanceCompleted(String pid, ICompletedResult result);

}
