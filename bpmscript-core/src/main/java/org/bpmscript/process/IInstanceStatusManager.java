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

import java.util.Collection;

import org.bpmscript.BpmScriptException;

/**
 * An interface for a service that reports how many live instances are running for 
 * a particular definition.
 */
public interface IInstanceStatusManager {
    
    /**
     * Get the live instances i.e. those that are CREATED, PAUSED or RUNNING but
     * not COMPLETED, FAILED or KILLED.
     *  
     * @param definitionId the definition whose instances may be running
     * @return the instances associated with the definition
     * @throws BpmScriptException if there was a problem looking up the instances
     *   associated with the definitionId.
     */
    Collection<String> getLiveInstances(String definitionId) throws BpmScriptException;
}
