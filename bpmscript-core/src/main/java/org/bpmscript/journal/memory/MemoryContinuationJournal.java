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

package org.bpmscript.journal.memory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.collections.MultiHashMap;
import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.journal.IContinuationJournalEntry;

/**
 * An in memory continuation journal.
 */
@SuppressWarnings("unchecked")
public class MemoryContinuationJournal implements IContinuationJournal {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private Map versionIndex = Collections.synchronizedMap(new MultiHashMap());
    private Map pidIndex = Collections.synchronizedMap(new MultiHashMap());
    private Map branchIndex = Collections.synchronizedMap(new MultiHashMap());
    private boolean clearOnComplete = true;
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

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
            if(entries == null) {
                log.debug("no entries found for " + pid);
                return Collections.emptyList();
            }
            HashSet<String> result = new HashSet<String>();
            for (MemoryContinuationJournalEntry entry : entries) {
                result.add(entry.getBranch());
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
            int size = entries.size();
            MemoryContinuationJournalEntry entry = (MemoryContinuationJournalEntry) entries.toArray()[size - 1];
            return entry.getProcessState();
        } finally {
            readLock.unlock();
        }
    }

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
     * @param pid the process id to clean up
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

}
