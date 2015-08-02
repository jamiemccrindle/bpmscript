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

import java.util.List;

import junit.framework.TestCase;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.exec.IgnoredResult;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.OrderBy;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;

/**
 * 
 */
public class MemoryInstanceManagerTest extends TestCase {

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#createInstance(java.lang.String, java.lang.String, java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testCreateInstance() throws BpmScriptException {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String pid = memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        assertNotNull(pid);
        PagedResult<IInstance> instancesForDefinition = memoryInstanceManager.getInstancesForDefinition(Query.ALL, definitionId);
        assertEquals(1, instancesForDefinition.getResults().size());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#doWithInstance(java.lang.String, org.bpmscript.process.IInstanceCallback)}.
     * @throws Exception 
     */
    public void testDoWithInstance() throws Exception {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String pid = memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        IExecutorResult result = memoryInstanceManager.doWithInstance(pid, new IInstanceCallback() {
        
            public IExecutorResult execute(IInstance processInstance) throws Exception {
                return new IgnoredResult("", "", "");
            }
        
        });
        assertNotNull(result);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#getInstancesForDefinition(org.bpmscript.paging.IQuery, java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetInstancesForDefinition() throws BpmScriptException {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String pid = memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        assertNotNull(pid);
        PagedResult<IInstance> instancesForDefinition = memoryInstanceManager.getInstancesForDefinition(Query.ALL, definitionId);
        assertEquals(1, instancesForDefinition.getResults().size());
        memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        instancesForDefinition = memoryInstanceManager.getInstancesForDefinition(Query.ALL, definitionId);
        assertEquals(2, instancesForDefinition.getResults().size());
        instancesForDefinition = memoryInstanceManager.getInstancesForDefinition(new Query((OrderBy[])null, 0, 10), definitionId);
        assertEquals(2, instancesForDefinition.getResults().size());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#getChildInstances(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetChildInstances() throws BpmScriptException {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String parentVersion = "blah";
        String pid = memoryInstanceManager.createInstance(parentVersion, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        assertNotNull(pid);
        List<IInstance> childInstances = memoryInstanceManager.getChildInstances(parentVersion);
        assertEquals(1, childInstances.size());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#getInstance(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetInstance() throws BpmScriptException {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String pid = memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        IInstance instance = memoryInstanceManager.getInstance(pid);
        assertNotNull(instance);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryInstanceManager#getInstances(org.bpmscript.paging.IQuery)}.
     * @throws BpmScriptException 
     */
    public void testGetInstances() throws BpmScriptException {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        String definitionId = "23452345";
        String pid = memoryInstanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
        IPagedResult<IInstance> instances = memoryInstanceManager.getInstances(Query.ALL);
        assertNotNull(instances);
        assertEquals(1, instances.getResults().size());
        assertEquals(pid, instances.getResults().get(0).getId());
    }

}
