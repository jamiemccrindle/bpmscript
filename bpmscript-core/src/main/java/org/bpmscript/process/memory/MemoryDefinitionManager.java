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
package org.bpmscript.process.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.exec.java.IJavaProcessDefinition;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IDefinitionManager;

/**
 * An in memory definition manager
 */
public class MemoryDefinitionManager implements IDefinitionManager {

	private Map<String, MemoryDefinition> memoryDefinitions = new ConcurrentHashMap<String, MemoryDefinition>();
	
	/**
	 * Create a new definition
	 */
	public void createDefinition(String definitionId, IProcessDefinition processDefinition) throws BpmScriptException {
		MemoryDefinition existingDefinition = memoryDefinitions.get(definitionId);
		if(existingDefinition == null) {
	        MemoryDefinition memoryDefinition = new MemoryDefinition();
	        memoryDefinition.setId(definitionId);
	        memoryDefinition.setName(processDefinition.getName());
	        memoryDefinition.setType(processDefinition.getType());
	        if (processDefinition instanceof IJavascriptProcessDefinition) {
	            IJavascriptProcessDefinition javascriptProcessDefinition = (IJavascriptProcessDefinition) processDefinition;
	            memoryDefinition.setSource(javascriptProcessDefinition.getSource());
            } else {
                IJavaProcessDefinition javaProcessDefinition = (IJavaProcessDefinition) processDefinition;
                memoryDefinition.setUrl(javaProcessDefinition.getUrl());
            }
	        memoryDefinitions.put(definitionId, memoryDefinition);
		}
	}

	/**
	 * Get the definition based on its definition id and null if there isn't one
	 */
	public IDefinition getDefinition(String definitionId) throws BpmScriptException {
		return memoryDefinitions.get(definitionId);
	}

	/**
	 * Gets all definitions that have the same name (i.e. different versions)
	 */
	public List<IDefinition> getDefinitionsByName(String name) throws BpmScriptException {
		List<IDefinition> result = new ArrayList<IDefinition>();
		for (Map.Entry<String, MemoryDefinition> entry : memoryDefinitions.entrySet()) {
			if(name.equals(entry.getValue().getName())) {
				result.add(entry.getValue());
			}
		}
		return result;
	}

}
