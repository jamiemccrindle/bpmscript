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

import java.util.List;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;

/**
 * The definition manager. Used to create and look up definitions.
 * 
 * @author jamie
 */
public interface IDefinitionManager {

    /**
     * Get a definition based on the definition id.
     * 
     * @param definitionId the definition id
     * @return the definition associated with the definition id
     * @throws BpmScriptException if something goes wrong
     */
    IDefinition getDefinition(String definitionId) throws BpmScriptException;
    
	/**
	 * Create a new definition with a particular name and set of source
	 * 
	 * @param definitionId the definition id
	 * @param processDefinition the script definition
	 * @throws BpmScriptException if something goes wrong
	 */
	void createDefinition(String definitionId, IProcessDefinition processDefinition) throws BpmScriptException;
	
	/**
	 * Get the definitions associated with a particular name.
	 * 
	 * @param name the name of the definitions
	 * @return a list of definitions with that name
	 * @throws BpmScriptException if something goes wrong
	 */
	List<IDefinition> getDefinitionsByName(String name) throws BpmScriptException;

}