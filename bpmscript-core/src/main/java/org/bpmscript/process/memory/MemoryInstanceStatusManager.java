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
import java.util.HashSet;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.IInstanceStatusManager;

/**
 * The Manager the combines process instances and their ProcessStates
 */
public class MemoryInstanceStatusManager implements IInstanceStatusManager {

    private IContinuationJournal continuationJournal;
    private IInstanceManager instanceManager;
    
    /**
     * @see org.bpmscript.process.IInstanceStatusManager#isLive(java.lang.String)
     */
    public Collection<String> getLiveInstances(String definitionId) throws BpmScriptException {
        final HashSet<String> result = new HashSet<String>();
        IPagedResult<IInstance> instances = instanceManager.getInstancesForDefinition(Query.ALL, definitionId);
        for (final IInstance instance : instances.getResults()) {
            try {
                Collection<String> branches = continuationJournal.getBranchesForPid(instance.getId());
                for (String branch : branches) {
                    ProcessState state = continuationJournal.getProcessStateLatest(branch);
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

    public void setContinuationJournal(IContinuationJournal continuationJournal) {
        this.continuationJournal = continuationJournal;
    }

    public void setInstanceManager(IInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

}
