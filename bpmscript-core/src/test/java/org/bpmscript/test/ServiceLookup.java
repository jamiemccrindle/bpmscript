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

package org.bpmscript.test;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class ServiceLookup implements IServiceLookup {

    private Map<String, Object> services = new HashMap<String, Object>();
    private IServiceLookup serviceLookup;
    
    public ServiceLookup(IServiceLookup serviceLookup) {
        this.serviceLookup = serviceLookup;
    }
    
    public ServiceLookup(IServiceLookup serviceLookup, String name, Object object) {
        this.serviceLookup = serviceLookup;
        this.addService(name, object);
    }
    
    public ServiceLookup() { }
    
    /* (non-Javadoc)
     * @see org.bpmscript.test.IServiceLookup#get(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        T result = (T) services.get(name);
        if(result == null && serviceLookup != null) {
            return (T) serviceLookup.get(name);
        } else {
            return result;
        }
    }
    
    public void addService(String name, Object object) {
        services.put(name, object);
    }

    public void setServices(Map<String, Object> services) {
        this.services = services;
    }

}
