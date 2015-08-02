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

package org.bpmscript.process.spring;

import java.util.HashMap;

import org.bpmscript.process.DefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfiguration;
import org.bpmscript.process.IDefinitionConfigurationLookup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Looks up properties in the application context
 */
public class ApplicationContextDefinitionConfigurationLookup implements IDefinitionConfigurationLookup, ApplicationContextAware {

    private ApplicationContext context;
    private String prefix = "";

    /**
     * @param applicationContext
     */
    public ApplicationContextDefinitionConfigurationLookup(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
    
    /**
     * @param prefix
     * @param applicationContext
     */
    public ApplicationContextDefinitionConfigurationLookup(String prefix, ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
    
    public ApplicationContextDefinitionConfigurationLookup() {
        
    }

    /**
     * @see org.bpmscript.process.IProperties#getProperty(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public IDefinitionConfiguration getDefinitionConfiguration(String definitionName) {
        if(context != null) {
            HashMap<String, IDefinitionConfiguration> beansOfType = new HashMap<String, IDefinitionConfiguration>();
            ApplicationContext temp = context;
            while(temp != null) {
                beansOfType.putAll(temp.getBeansOfType(IDefinitionConfiguration.class));
                temp = temp.getParent();
            }
            Object result = beansOfType.get(prefix + definitionName);
            if(result != null) {
                return (IDefinitionConfiguration) result;
            }
        }
        return new DefinitionConfiguration();
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
