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

package org.bpmscript.process.listener;

import org.bpmscript.ICompletedResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IInstanceListener;

/**
 * Logs out information about instances being created, pausing, completing, failing
 * or being killed.
 */
public class LoggingInstanceListener implements IInstanceListener {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    public void instanceCompleted(String pid, ICompletedResult result) {
        log.debug("completed " + pid);
    }

    public void instanceFailed(String pid, IFailedResult result) {
        Throwable throwable = result.getThrowable();
        log.error(throwable, throwable);
    }
    
    public void instancePaused(String pid, IPausedResult result) {
        log.debug("paused " + pid);
    }
    
    public void instanceCreated(String pid, IDefinition definition) {
        log.debug("created " + pid + " with name " + definition.getName());
    }

    public void instanceKilled(String pid, IKilledResult result) {
        log.debug("killed " + pid + " with message " + result.getMessage());
    }

}
