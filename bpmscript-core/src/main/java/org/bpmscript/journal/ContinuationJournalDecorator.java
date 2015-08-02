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

package org.bpmscript.journal;

import java.util.Collection;

import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;

/**
 * Decorator class for contination journals
 */
public class ContinuationJournalDecorator {
    
    private IContinuationJournal delegate;
    
    public ContinuationJournalDecorator(IContinuationJournal delegate) {
        this.delegate = delegate;
    }
    
    public ContinuationJournalDecorator() {
        
    }

    public String createBranch(String version) {
        return delegate.createBranch(version);
    }

    public String createMainBranch(String pid) {
        return delegate.createMainBranch(pid);
    }

    public String createVersion(String pid, String branch) {
        return delegate.createVersion(pid, branch);
    }

    public Collection<String> getBranchesForPid(String pid) {
        return delegate.getBranchesForPid(pid);
    }

    public byte[] getContinuationLatest(String branch) {
        return delegate.getContinuationLatest(branch);
    }

    public Collection<IContinuationJournalEntry> getEntriesForPid(String pid) {
        return delegate.getEntriesForPid(pid);
    }

    public Collection<IContinuationJournalEntry> getLiveResults(String pid) {
        return delegate.getLiveResults(pid);
    }

    public ProcessState getProcessStateLatest(String branch) {
        return delegate.getProcessStateLatest(branch);
    }

    public String getVersionLatest(String branch) {
        return delegate.getVersionLatest(branch);
    }

    public void storeResult(byte[] continuation, IExecutorResult result) {
        delegate.storeResult(continuation, result);
    }

    public IContinuationJournal getDelegate() {
        return delegate;
    }

    public void setDelegate(IContinuationJournal delegate) {
        this.delegate = delegate;
    }
}
