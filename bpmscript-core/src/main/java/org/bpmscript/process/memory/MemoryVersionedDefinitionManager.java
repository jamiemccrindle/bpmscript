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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IDefinitionManager;
import org.bpmscript.process.IVersionedDefinitionManager;

/**
 * Implementation of {@link IVersionedDefinitionManager} that keeps the list of primary
 * definitions in memory.
 */
public class MemoryVersionedDefinitionManager implements IVersionedDefinitionManager {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

	private IDefinitionManager definitionManager = null;
	private Map<String, IDefinition> versions = new ConcurrentHashMap<String, IDefinition>();
	
	public MemoryVersionedDefinitionManager() {
        super();
    }

    public MemoryVersionedDefinitionManager(IDefinitionManager definitionManager) {
        super();
        this.definitionManager = definitionManager;
    }

    /**
     * Set a definition as the primary definition
     */
    public void setDefinitionAsPrimary(String definitionId) throws BpmScriptException {
		IDefinition definition = definitionManager.getDefinition(definitionId);
		versions.put(definition.getName(), definition);
	}
	
    /**
     * Return the definition that was set as primary, null if there isn't a primary
     * definition for definitions with this name.
     */
	public IDefinition getPrimaryDefinition(String name) throws BpmScriptException {
		return versions.get(name);
	}

	/**
	 * Get a list of all the primary definitions
	 */
	public List<IDefinition> getPrimaryDefinitions() throws BpmScriptException {
		ArrayList<IDefinition> definitions = new ArrayList<IDefinition>(versions.values());
		Collections.sort(definitions, new Comparator<IDefinition>() {
        
            public int compare(IDefinition d1, IDefinition d2) {
                return d1.getName().compareTo(d2.getName());
            }
        });
		return definitions;
	}

	public void setDefinitionManager(IDefinitionManager definitionManager) {
		this.definitionManager = definitionManager;
	}

	/**
	 * Create a new definition with the associated id and process definition
	 */
	public void createDefinition(String id, IProcessDefinition processDefinition) throws BpmScriptException {
	    log.info("creating definition " + processDefinition.getName());
		definitionManager.createDefinition(id, processDefinition);
		IDefinition definition = versions.get(processDefinition.getName());
		if(definition == null) {
			versions.put(processDefinition.getName(), definitionManager.getDefinition(id));
		}
	}

	/**
	 * Delegates to a definition manager
	 */
	public IDefinition getDefinition(String definitionId) throws BpmScriptException {
		return definitionManager.getDefinition(definitionId);
	}

    /**
     * Delegates to a definition manager
     */
	public List<IDefinition> getDefinitionsByName(String name) throws BpmScriptException {
		return definitionManager.getDefinitionsByName(name);
	}


}
