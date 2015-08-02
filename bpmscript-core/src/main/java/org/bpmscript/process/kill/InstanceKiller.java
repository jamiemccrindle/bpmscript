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

package org.bpmscript.process.kill;

import java.util.Collection;

import org.bpmscript.BpmScriptException;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.process.IBpmScriptEngine;
import org.bpmscript.process.IInstanceKiller;
import org.bpmscript.process.IInstanceStatusManager;

/**
 * Instance killer
 */
public class InstanceKiller implements IInstanceKiller {

    private IContinuationJournal continuationJournal;
    private IInstanceStatusManager instanceStatusManager;
    private IBpmScriptEngine bpmScriptEngine;
    
    /**
     * Kill all instances associated with a definition id
     */
    public int killDefinitionInstances(String definitionId, String message) throws BpmScriptException {
        Collection<String> liveInstances = instanceStatusManager.getLiveInstances(definitionId);
        for (String pid : liveInstances) {
            killInstance(pid, message);
        }
        return liveInstances.size();
    }

    /**
     * Kill a particular instance
     */
    public void killInstance(String pid, String message) throws BpmScriptException {
        Collection<String> branches = continuationJournal.getBranchesForPid(pid);
        for (String branch : branches) {
            bpmScriptEngine.kill(pid, branch, message);
        }
    }

    public void setContinuationJournal(IContinuationJournal continuationJournal) {
        this.continuationJournal = continuationJournal;
    }

    public void setBpmScriptEngine(IBpmScriptEngine bpmScriptEngine) {
        this.bpmScriptEngine = bpmScriptEngine;
    }

    public void setInstanceStatusManager(IInstanceStatusManager instanceStatusManager) {
        this.instanceStatusManager = instanceStatusManager;
    }

}
