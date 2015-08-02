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
 * A BpmScript request. Represents the parameters that are used to call the {@link IBpmScriptEngine}
 * sendFirst method
 * 
 * @see IBpmScriptEngine#sendFirst(String, String, String, Object)
 */
public interface IBpmScriptRequest {
    
    /**
     * @return the parent version if there is one
     */
    String getParentVersion();
    
    /**
     * @return the name of the definition to run.
     */
    String getDefinitionName();
    
    /**
     * @return the operation to run
     */
    String getOperation();
    
    /**
     * @return the message to send
     */
    Object getMessage();
}
