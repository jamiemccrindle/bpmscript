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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.collections.MultiHashMap;
import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.journal.IContinuationJournalEntry;
import org.bpmscript.journal.memory.MemoryContinuationJournalEntry;
import org.bpmscript.paging.IOrderBy;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.IInstanceStatusManager;
import org.bpmscript.process.lock.LockingInstanceManager;
import org.bpmscript.util.ReflectionComparator;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class MemoryBpmScriptManager implements IInstanceManager, IContinuationJournal, IInstanceStatusManager {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private Map<String, MemoryInstance> memoryInstances = Collections.synchronizedMap(new LinkedHashMap<String, MemoryInstance>(16, 0.75F, true));
    private Map versionIndex = Collections.synchronizedMap(new MultiHashMap());
    private Map pidIndex = Collections.synchronizedMap(new MultiHashMap());
    private Map branchIndex = Collections.synchronizedMap(new MultiHashMap());
    private boolean clearOnComplete = true;
    private int maxEntries = 1000;
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static Map<String, AtomicInteger> locks = new ConcurrentHashMap<String, AtomicInteger>();
    
    /**
     * @see org.bpmscript.journal.IContinuationJournal#createBranch(java.lang.String)
     */
    public String createBranch(String version) {
        MemoryContinuationJournalEntry currentEntry = null;
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            currentEntry = (MemoryContinuationJournalEntry) ((Collection) versionIndex
                    .get(version)).iterator().next();
        } finally {
            readLock.unlock();
        }
        String branch = UUID.randomUUID().toString();
        MemoryContinuationJournalEntry newEntry = new MemoryContinuationJournalEntry();
        newEntry.setId(UUID.randomUUID().toString());
        newEntry.setContinuation(currentEntry.getContinuation());
        newEntry.setInstanceId(currentEntry.getInstanceId());
        newEntry.setProcessState(currentEntry.getProcessState());
        newEntry.setVersion(version);
        newEntry.setBranch(branch);
        newEntry.setLastModified(new Timestamp(System.currentTimeMillis()));
        storeEntry(newEntry);
        return branch;
    }

    /**
     * Stores a continuation entry. Used wherever a continuation entry is created so that all the
     * indexes are populated.
     * 
     * @param entry the entry to store
     */
    public void storeEntry(MemoryContinuationJournalEntry entry) {
        WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            versionIndex.put(entry.getVersion(), entry);
            pidIndex.put(entry.getInstanceId(), entry);
            branchIndex.put(entry.getBranch(), entry);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#createMainBranch(java.lang.String)
     */
    public String createMainBranch(String pid) {
        String branch = UUID.randomUUID().toString();
        return branch;
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#createVersion(java.lang.String,
     *      java.lang.String)
     */
    public String createVersion(String pid, String branch) {
        return UUID.randomUUID().toString();
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getBranchesForPid(java.lang.String)
     */
    public Collection<String> getBranchesForPid(String pid) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            Collection<MemoryContinuationJournalEntry> entries = ((Collection<MemoryContinuationJournalEntry>) pidIndex
                    .get(pid));
            HashSet<String> result = new HashSet<String>();
            if(entries != null) {
                for (MemoryContinuationJournalEntry entry : entries) {
                    result.add(entry.getBranch());
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getContinuationLatest(java.lang.String)
     */
    public byte[] getContinuationLatest(String branch) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            Collection entries = ((Collection) branchIndex.get(branch));
            if (entries != null) {
                int size = entries.size();
                MemoryContinuationJournalEntry entry = (MemoryContinuationJournalEntry) entries.toArray()[size - 1];
                return entry.getContinuation();
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getLiveResults(java.lang.String)
     */
    public Collection<IContinuationJournalEntry> getLiveResults(String pid) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            Collection<IContinuationJournalEntry> entries = (Collection<IContinuationJournalEntry>) pidIndex.get(pid);
            ArrayList<IContinuationJournalEntry> result = new ArrayList<IContinuationJournalEntry>();
            for (IContinuationJournalEntry entry : entries) {
                switch (entry.getProcessState()) {
                case CREATED:
                case RUNNING:
                case PAUSED:
                    result.add(entry);
                    break;
                case COMPLETED:
                case FAILED:
                case KILLED:
                    break;
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getProcessStateLatest(java.lang.String)
     */
    public ProcessState getProcessStateLatest(String branch) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            Collection entries = ((Collection) branchIndex.get(branch));
            if(entries == null) return null;
            int size = entries.size();
            if(size == 0) return null;
            MemoryContinuationJournalEntry entry = (MemoryContinuationJournalEntry) entries.toArray()[size - 1];
            return entry.getProcessState();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#isDirty(java.lang.String)
     */
    public String getVersionLatest(String branch) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            Collection entries = ((Collection) branchIndex.get(branch));
            int size = entries.size();
            MemoryContinuationJournalEntry entry = (MemoryContinuationJournalEntry) entries.toArray()[size - 1];
            return entry.getVersion();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#storeResult(byte[],
     *      org.bpmscript.IExecutorResult)
     */
    public void storeResult(byte[] continuation, IExecutorResult result) {
        if (clearOnComplete && result.getProcessState() != ProcessState.PAUSED) {
            cleanUpEntries(result.getPid());
        } else {
            MemoryContinuationJournalEntry entry = new MemoryContinuationJournalEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setBranch(result.getBranch());
            entry.setContinuation(continuation);
            entry.setInstanceId(result.getPid());
            entry.setLastModified(new Timestamp(System.currentTimeMillis()));
            entry.setProcessState(result.getProcessState());
            entry.setVersion(result.getVersion());
            storeEntry(entry);
        }
    }

    /**
     * This method cleans up any entries related to a branch
     * 
     * @param result
     */
    public void cleanUpEntries(String pid) {
        WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            Collection pidEntries = (Collection) pidIndex.get(pid);
            if(pidEntries != null) {
                for (Iterator iterator = pidEntries.iterator(); iterator.hasNext();) {
                    MemoryContinuationJournalEntry entry = (MemoryContinuationJournalEntry) iterator.next();
                    versionIndex.remove(entry.getVersion());
                    branchIndex.remove(entry.getBranch());
                }
                pidIndex.remove(pid);
                memoryInstances.remove(pid);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getResults(java.lang.String)
     */
    public Collection<IContinuationJournalEntry> getEntriesForPid(String pid) {
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            return (Collection<IContinuationJournalEntry>) pidIndex.get(pid);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Create a new in memory instance
     */
    public String createInstance(String parentVersion, String definitionId, String definitionName, String definitionType, String operation) throws BpmScriptException {
        
        synchronized(memoryInstances) {
            int memoryInstancesSize = memoryInstances.size();
            if(memoryInstancesSize > maxEntries && maxEntries > 0) {
                ArrayList<String> pidsToRemove = new ArrayList<String>();
                Iterator<String> iterator = memoryInstances.keySet().iterator();
                do {
                    pidsToRemove.add(iterator.next());
                } while(memoryInstancesSize-- > maxEntries);
                for (String pid : pidsToRemove) {
                    log.debug("removing pid " + pid + " as maxEntries has been exceeded");
                    cleanUpEntries(pid);
                }
            }
        }
        
        String id = UUID.randomUUID().toString();
        MemoryInstance memoryInstance = new MemoryInstance();
        memoryInstance.setId(id);
        memoryInstance.setOperation(operation);
        memoryInstance.setParentVersion(parentVersion);
        memoryInstance.setDefinitionId(definitionId);
        memoryInstance.setDefinitionName(definitionName);
        memoryInstance.setDefinitionType(definitionType);
        memoryInstances.put(id, memoryInstance);
        return id;
    }

    /**
     * Call the callback with the instance reference by the pid. Should be used in
     * conjunction with a locking mechanism, e.g. LockingInstanceManager
     * 
     * @see LockingInstanceManager
     */
    public IExecutorResult doWithInstance(String processInstanceId, IInstanceCallback callback) throws Exception {
        AtomicInteger lock = null;
        synchronized (locks) {
            lock = locks.get(processInstanceId);
            if (lock == null) {
                lock = new AtomicInteger(1);
                locks.put(processInstanceId, lock);
            } else {
                lock.set(lock.get() + 1);
            }
        }
        IExecutorResult result = null;
        synchronized (lock) {
            try {
                log.debug("locking " + processInstanceId + " " + lock);
                MemoryInstance processInstance = memoryInstances.get(processInstanceId);
                result = callback.execute(processInstance);
                if(result.getProcessState() != ProcessState.IGNORED) {
                    processInstance.setLastModified(new Timestamp(System.currentTimeMillis()));
                }
            } finally {
                log.debug("unlocking " + processInstanceId);
                lock.set(lock.get() - 1);
                if(lock.get() == 0) {
                    locks.remove(processInstanceId);
                }
            }
        }
        return result;
        
    }

    /**
     * Get all instances associated with a definition
     */
    public PagedResult<IInstance> getInstancesForDefinition(IQuery query, String definitionId) throws BpmScriptException {
        List<IInstance> results = new ArrayList<IInstance>();
        for (Map.Entry<String, MemoryInstance> entry : memoryInstances.entrySet()) {
            if(definitionId.equals(entry.getValue().getDefinitionId())) {
                results.add(entry.getValue());
            }
        }
        int totalResults = results.size();
        sortResults(results, query.getOrderBys());
        if(query.getMaxResults() > 0) {
            int maxToGet = query.getFirstResult() + query.getMaxResults() + 1;
            results = results.subList(query.getFirstResult(), results.size() > maxToGet ? maxToGet : results.size());
        }
        return new PagedResult<IInstance>(results, query.getMaxResults(), totalResults);
    }

    /**
     * Get child instances
     */
    public List<IInstance> getChildInstances(String parentVersion) throws BpmScriptException {
        List<IInstance> result = new ArrayList<IInstance>();
        for (Map.Entry<String, MemoryInstance> entry : memoryInstances.entrySet()) {
            if(parentVersion.equals(entry.getValue().getParentVersion())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get instance for a pid
     */
    public IInstance getInstance(String pid) throws BpmScriptException {
        return memoryInstances.get(pid);
    }

    /**
     * Get all instances limited by a query, in a paged form
     */
    public IPagedResult<IInstance> getInstances(IQuery query) throws BpmScriptException {
        List<IInstance> results = new ArrayList<IInstance>(memoryInstances.values());

        sortResults(results, query.getOrderBys());
        List<IInstance> trimmedResults = null;
        int lastResult = query.getFirstResult() + query.getMaxResults();

        if(query.getFirstResult() >= 0 && query.getMaxResults() >= 0) {
            trimmedResults = new ArrayList<IInstance>(results.subList(query.getFirstResult(), lastResult < results.size() ? lastResult : results.size())); 
        } else {
            trimmedResults = results;
        }

        return new PagedResult<IInstance>(trimmedResults, lastResult > 0 && lastResult < results.size(), results.size());
    }

    /**
     * @param results the results to sort
     * @param orderBys the properties to sort by
     * @throws BpmScriptException if something goes wrong (e.g. one of the properties is not valid)
     */
    private void sortResults(List<IInstance> results, final List<IOrderBy> orderBys) throws BpmScriptException {
        if(orderBys != null && orderBys.size() > 0) {
            try {
                Collections.sort(results, new ReflectionComparator(orderBys));
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();
                if(cause != null && cause instanceof BpmScriptException) {
                    throw (BpmScriptException) cause;
                } else {
                    throw e;
                }
            }
        }
    }
    
    /**
     * @see org.bpmscript.process.IInstanceStatusManager#isLive(java.lang.String)
     */
    public Collection<String> getLiveInstances(String definitionId) throws BpmScriptException {
        final HashSet<String> result = new HashSet<String>();
        IPagedResult<IInstance> instances = this.getInstancesForDefinition(Query.ALL, definitionId);
        for (final IInstance instance : instances.getResults()) {
            try {
                Collection<String> branches = this.getBranchesForPid(instance.getId());
                for (String branch : branches) {
                    ProcessState state = this.getProcessStateLatest(branch);
                    if(state == ProcessState.CREATED ||
                            state == ProcessState.RUNNING ||
                            state == ProcessState.PAUSED) {
                        result.add(instance.getId());
                    }
                }
            } catch (Throwable e) {
                throw new BpmScriptException(e);
            }
        }
        return result;
    }

}
