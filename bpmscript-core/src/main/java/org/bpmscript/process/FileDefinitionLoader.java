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

import java.util.Map;

import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.util.StreamService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Loads definitions from an explicit set of file paths
 */
public class FileDefinitionLoader implements InitializingBean {
    
    private IVersionedDefinitionManager versionedDefinitionManager;
    private IBpmScriptEngine engine;
    private Map<String, Resource> definitions;   
    
    /**
     * @param versionedDefinitionManager the versionedDefinitionManager to load
     *  the new definitions into
     */
    public void setVersionedDefinitionManager(IVersionedDefinitionManager versionedDefinitionManager) {
        this.versionedDefinitionManager = versionedDefinitionManager;
    }

    /**
     * Goes through each of the definitions and loads them into the definition manager
     */
    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<String, Resource> entry : definitions.entrySet()) {
            String name = entry.getKey();
            String source = StreamService.DEFAULT_INSTANCE.readFully(entry.getValue().getInputStream());
            JavascriptProcessDefinition javascriptProcessDefinition = new JavascriptProcessDefinition(name, source);
            String id = engine.validate(javascriptProcessDefinition);
            versionedDefinitionManager.createDefinition(id, javascriptProcessDefinition);
        }
    }

    /**
     * @param definitions a map of definition name to spring resource
     */
    public void setDefinitions(Map<String, Resource> definitions) {
        this.definitions = definitions;
    }

    /**
     * @param engine the engine that is used for validating the scripts
     */
    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

}
