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

/**
 * A facade for calling BpmScript processes like normal objects. Processes can either
 * be called synchronously using "call" and asynchronously (one way) using "callAsync"
 */
public interface IBpmScriptFacade {

    /**
     * Call a BpmScript definition synchronously 
     * 
     * @param definitionName the name of the definition to call
     * @param methodName the method on the definition to call
     * @param timeout how long to wait for
     * @param args the arguments for the definition
     * @return the result of a "reply" from the definition
     * @throws Exception if the BpmScript instance replies with an exception
     */
    public Object call(String definitionName, String methodName, long timeout, Object... args) throws Exception;

    /**
     * Does a one way send to a BpmScript process.
     *  
     * @param definitionName the name of the definition to call
     * @param methodName the method on the definition to call
     * @param args the arguments for the definition
     * @throws Exception if the BpmScript instance replies with an exception
     */
    public void callAsync(String definitionName, String methodName, Object... args) throws Exception;
}
