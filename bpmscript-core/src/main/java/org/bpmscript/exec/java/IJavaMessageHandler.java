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

package org.bpmscript.exec.java;

import java.io.Serializable;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;

/**
 * The Java Message Handler interface used to either receive messages or 
 * timeouts.
 * 
 * There is nothing stopping a timeout happening even though the original
 * message has been received so care must be taken to ignore the timeout 
 * after the message is received.
 */
public interface IJavaMessageHandler<MESSAGE_TYPE> extends Serializable {
    
    /**
     * Process a message and return whether the instance should complete
     * or continue (pause)
     * 
     * @param message the message that is received
     * @return whether the instance should be PAUSED or COMPLETED
     * @throws BpmScriptException if something goes wrong
     */
    ProcessState process(MESSAGE_TYPE message) throws BpmScriptException;
    
    /**
     * Handle a timeout
     * 
     * @return whether the instance should be PAUSED or COMPLETED
     * @throws BpmScriptException if something goes wrong
     */
    ProcessState timeout() throws BpmScriptException;
}
