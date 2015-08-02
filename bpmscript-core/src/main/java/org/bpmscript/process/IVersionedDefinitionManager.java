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

/**
 * An extension of the {@link IDefinitionManager} that supports the concept of
 * having a number of versions of a definition, one of which being the primary
 * definition.
 */
public interface IVersionedDefinitionManager extends IDefinitionManager {
    
    /**
     * Set one of the definitions as the primary definition
     * @param definitionId the definition id to set as primary
     * @throws BpmScriptException if there was a problem setting this definition
     *   as the primary one e.g. if it doesn't exist.
     */
	void setDefinitionAsPrimary(String definitionId) throws BpmScriptException;
	
	/**
	 * @return get the list of definitions set as primary 
	 * @throws BpmScriptException if there was a problem retrieving the list
	 */
	List<IDefinition> getPrimaryDefinitions() throws BpmScriptException;
	
	/**
	 * Get the primary definition for the definition name
	 * @param name the name of the definition
	 * @return the primary definition for definitions which share this name
	 * @throws BpmScriptException if there was a problem retrieving the primary
	 *   definition.
	 */
	IDefinition getPrimaryDefinition(String name) throws BpmScriptException;
}
