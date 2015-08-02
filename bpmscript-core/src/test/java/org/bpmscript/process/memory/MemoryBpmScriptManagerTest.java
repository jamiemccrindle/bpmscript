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

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;
import org.bpmscript.exec.PausedResult;
import org.bpmscript.journal.IContinuationJournalEntry;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;

/**
 * 
 */
public class MemoryBpmScriptManagerTest extends TestCase {

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#createBranch(java.lang.String)}.
     */
    public void testCreateBranch() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        assertNotNull(version);
        String newBranch = store.createBranch(version);
        assertNotNull(newBranch);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#storeEntry(org.bpmscript.journal.memory.MemoryContinuationJournalEntry)}.
     */
    public void testStoreEntry() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#createMainBranch(java.lang.String)}.
     */
    public void testCreateMainBranch() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        assertNotNull(mainBranch);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#createVersion(java.lang.String, java.lang.String)}.
     */
    public void testCreateVersion() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String version = store.createVersion("test", "test");
        assertNotNull(version);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getBranchesForPid(java.lang.String)}.
     */
    public void testGetBranchesForPid() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        assertNotNull(version);
        String newBranch = store.createBranch(version);
        assertNotNull(newBranch);
        Collection<String> branchesForPid = store.getBranchesForPid(pid);
        assertNotNull(branchesForPid);
        assertEquals(2, branchesForPid.size());
        assertTrue(branchesForPid.contains(mainBranch));
        assertTrue(branchesForPid.contains(newBranch));
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getContinuationLatest(java.lang.String)}.
     */
    public void testGetContinuationLatest() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult("sdf".getBytes(), new PausedResult(pid, mainBranch, version, "sdf"));
        byte[] continuationLatest = store.getContinuationLatest(mainBranch);
        assertEquals("sdf", new String(continuationLatest));
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getLiveResults(java.lang.String)}.
     */
    public void testGetLiveResults() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        Collection<IContinuationJournalEntry> liveResults = store.getLiveResults(pid);
        assertNotNull(liveResults);
        assertEquals(1, liveResults.size());
        IContinuationJournalEntry next = liveResults.iterator().next();
        assertNotNull(next);
        assertEquals(pid, next.getPid());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getProcessStateLatest(java.lang.String)}.
     */
    public void testGetProcessStateLatest() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult("sdf".getBytes(), new PausedResult(pid, mainBranch, version, "sdf"));
        ProcessState stateLatest = store.getProcessStateLatest(mainBranch);
        assertEquals(ProcessState.PAUSED, stateLatest);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getVersionLatest(java.lang.String)}.
     */
    public void testGetVersionLatest() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult("sdf".getBytes(), new PausedResult(pid, mainBranch, version, "sdf"));
            String versionLatest = store.getVersionLatest(mainBranch);
            assertEquals(version, versionLatest);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult("sdf".getBytes(), new PausedResult(pid, mainBranch, version, "sdf"));
            String versionLatest = store.getVersionLatest(mainBranch);
            assertEquals(version, versionLatest);
        }
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#storeResult(byte[], org.bpmscript.IExecutorResult)}.
     */
    public void testStoreResult() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
        {
            String version = store.createVersion(pid, mainBranch);
            store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
            assertNotNull(version);
        }
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#cleanUpEntries(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testCleanUpEntries() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        store.cleanUpEntries(pid);
        IInstance instance = store.getInstance(pid);
        assertNull(instance);
        Collection<String> branchesForPid = store.getBranchesForPid(pid);
        assertEquals(0, branchesForPid.size());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getEntriesForPid(java.lang.String)}.
     */
    public void testGetEntriesForPid() {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        Collection<IContinuationJournalEntry> entriesForPid = store.getEntriesForPid(pid);
        assertNotNull(entriesForPid);
        assertEquals(1, entriesForPid.size());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#createInstance(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testCreateInstance() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        String pid = store.createInstance(null, "definitionId", "definitionName", "definitionType", "operation");
        assertNotNull(pid);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#doWithInstance(java.lang.String, org.bpmscript.process.IInstanceCallback)}.
     * @throws Exception 
     */
    public void testDoWithInstance() throws Exception {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance(null, "definitionId", "definitionName", "definitionType", "operation");
        IExecutorResult result = store.doWithInstance(pid, new IInstanceCallback() {
            public IExecutorResult execute(IInstance processInstance) throws Exception {
                return new PausedResult(pid, "test", "test", "test");
            }
        });
        assertEquals(pid, result.getPid());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getInstancesForDefinition(org.bpmscript.paging.IQuery, java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetInstancesForDefinition() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance(null, "definitionId", "definitionName", "definitionType", "operation");
        PagedResult<IInstance> instances = store.getInstancesForDefinition(Query.ALL, "definitionId");
        assertNotNull(instances);
        List<IInstance> results = instances.getResults();
        assertEquals(1, results.size());
        assertEquals(pid, results.get(0).getId());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getChildInstances(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetChildInstances() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance("random", "definitionId", "definitionName", "definitionType", "operation");
        List<IInstance> instances = store.getChildInstances("random");
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(pid, instances.get(0).getId());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getInstance(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetInstance() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance("random", "definitionId", "definitionName", "definitionType", "operation");
        IInstance instance = store.getInstance(pid);
        assertNotNull(instance);
        assertEquals(pid, instance.getId());
        IInstance instance2 = store.getInstance("notfound");
        assertNull(instance2);
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getInstances(org.bpmscript.paging.IQuery)}.
     * @throws BpmScriptException 
     */
    public void testGetInstances() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance("random", "definitionId", "definitionName", "definitionType", "operation");
        IPagedResult<IInstance> instances = store.getInstances(Query.ALL);
        assertNotNull(instances);
        List<IInstance> results = instances.getResults();
        assertEquals(1, results.size());
        assertEquals(pid, results.get(0).getId());
    }

    /**
     * Test method for {@link org.bpmscript.process.memory.MemoryBpmScriptManager#getLiveInstances(java.lang.String)}.
     * @throws BpmScriptException 
     */
    public void testGetLiveInstances() throws BpmScriptException {
        MemoryBpmScriptManager store = new MemoryBpmScriptManager();
        final String pid = store.createInstance("random", "definitionId", "definitionName", "definitionType", "operation");
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        Collection<String> instances = store.getLiveInstances("definitionId");
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(pid, instances.iterator().next());
    }

}
