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
import java.util.Map;


/**
 * Spring wireable definition configuration
 */
public class DefinitionConfiguration implements IDefinitionConfiguration {

    private Map<String,Object> properties = new HashMap<String, Object>();
    
    /**
     * Gets a property value based on the name
     */
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * Gets the properties for this definition
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties for this definition
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
