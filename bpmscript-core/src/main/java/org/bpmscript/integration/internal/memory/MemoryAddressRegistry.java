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
package org.bpmscript.integration.internal.memory;

import java.util.HashMap;
import java.util.Map;

import org.bpmscript.integration.internal.IMessageReceiver;
import org.bpmscript.integration.internal.IMutableAddressRegistry;

/**
 * An in memory implementation of an address registry
 */
public class MemoryAddressRegistry implements IMutableAddressRegistry {

    private Map<String, IMessageReceiver> endpoints = new HashMap<String, IMessageReceiver>();
    
    public IMessageReceiver lookup(String address) {
        return endpoints.get(address);
    }

    public void setEndpoints(Map<String, IMessageReceiver> endpoints) {
        this.endpoints = endpoints;
    }

    public void register(String address, IMessageReceiver endpoint) {
        endpoints.put(address, endpoint);
    }

}
