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

import java.util.HashMap;

import org.bpmscript.IProcessExecutor;

/**
 * Keeps a map of types to process executors
 */
public class ProcessExecutorLookup implements IProcessExecutorLookup {

    private HashMap<String, IProcessExecutor> processExecutors = new HashMap<String, IProcessExecutor>();
    
    /**
     * @see org.bpmscript.process.IProcessExecutorLookup#lookup(java.lang.String)
     */
    public IProcessExecutor lookup(String type) {
        return processExecutors.get(type);
    }

    /**
     * @param processExecutors the map of definition types to process executors
     */
    public void setProcessExecutors(HashMap<String, IProcessExecutor> processExecutors) {
        this.processExecutors = processExecutors;
    }
    
    /**
     * Add a single type to {@link IProcessExecutor} mapping
     * 
     * @param type the definition type
     * @param processExecutor the {@link IProcessExecutor} capable of running definitions
     *   of that type.
     */
    public void addProcessExecutor(String type, IProcessExecutor processExecutor) {
        this.processExecutors.put(type, processExecutor);
    }

}
