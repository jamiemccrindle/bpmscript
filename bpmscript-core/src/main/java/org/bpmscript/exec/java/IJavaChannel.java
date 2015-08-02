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

import org.apache.commons.logging.Log;
import org.bpmscript.BpmScriptException;

/**
 * JavaChannel interface. Java Channels are stateful and are typically serialised
 * after they've done a "receive". There is a bidirectional relationship between
 * the JavaChannel and the definition object stored within them. 
 */
public interface IJavaChannel {
    
    /**
     * Send a message, return the result to the handler or call the timeout method
     * on the handler if the timeout runs out...
     * 
     * @param message the message to send
     * @param timeout how long to wait before timing out
     * @param handler the handler to call either when a message comes in or a timeout occurs
     * @throws BpmScriptException if something goes wrong sending the message
     */
    void send(Object message, long timeout, IJavaMessageHandler<?> handler) throws BpmScriptException;
    
    /**
     * Reply to a message
     * 
     * @param message
     * @throws BpmScriptException
     */
    void reply(Object message) throws BpmScriptException;
    
    /**
     * @return Get the parent version of the process (if it has one) 
     */
    String getParentVersion();
    
    /**
     * @return the instance id associated with thie process instance
     */
    String getPid();
    
    /**
     * @return the request that kicked off this process instance...
     */
    Object getRequest();
    
    /**
     * Gets the content associated with a message
     * @param message the message to unwrap
     * @return the content associated with a message
     */
    Object getContent(Object message);
    
    /**
     * Set a logger onto this channel
     * 
     * @param log a logger
     */
    void setLog(Log log);
}