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

import org.bpmscript.BpmScriptException;

/**
 * An interface for services that send "kill" messages to instances
 */
public interface IInstanceKiller {
    
    /**
     * Kill a particular instance and all of its branches
     * 
     * @param pid the instance to kill
     * @param message the reason that it was killed
     * @throws BpmScriptException if there was a problem killing it e.g. the 
     *  instance could not be found.
     */
    void killInstance(String pid, String message) throws BpmScriptException;
    
    /**
     * Kill all instances and all of their branches associated with a definition.
     * 
     * @param definitionId the definition id
     * @param message the reason for killing the instances
     * @return how many instances were killed
     * @throws BpmScriptException if there was a problem killing the instances.
     */
    int killDefinitionInstances(String definitionId, String message) throws BpmScriptException;
}
