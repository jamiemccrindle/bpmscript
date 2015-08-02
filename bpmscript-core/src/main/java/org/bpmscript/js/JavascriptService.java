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

package org.bpmscript.js;

import java.util.HashMap;
import java.util.Map;

/**
 * Javascript service contains enough information to create itself as a service...
 */
public class JavascriptService implements IJavascriptService {
    
    private String source;
    private String name;
    private Map<String, Object> properties;
    
    public JavascriptService() {}
    
    public JavascriptService(String source) {
        this.source = source;
    }
    
    /* (non-Javadoc)
     * @see org.bpmscript.js.IJavascriptService#getName()
     */
    public String getName() {
        if(name == null) {
            return titleCase(source.substring(source.lastIndexOf("/") + 1, source.lastIndexOf(".")));
        }
        return name;
    }
    
    public String titleCase(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
    
    public void addProperty(String key, Object value) {
        if(properties == null) {
            properties = new HashMap<String, Object>();
        }
        this.properties.put(key, value);
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.js.IJavascriptService#getSource()
     */
    public String getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.js.IJavascriptService#getProperties()
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
