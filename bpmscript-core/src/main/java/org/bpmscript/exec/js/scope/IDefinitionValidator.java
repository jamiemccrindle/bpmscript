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

package org.bpmscript.exec.js.scope;

import java.util.Map;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.mozilla.javascript.Context;

/**
 * Validate a Javascript script definition
 */
public interface IDefinitionValidator {
    
    /**
     * Validate a scope definition. The validator should return a unique id for
     * this process definition.
     * 
     * @param processDefinition the process definition to validate
     * @param invocationContext and context objects
     * @param cx the Javascript contxt
     * @return a unique identifier
     * @throws BpmScriptException
     */
    String validate(IProcessDefinition processDefinition,
            Map<String, Object> invocationContext, Context cx) throws
            BpmScriptException;
}
