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

package org.bpmscript.audit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronously sends messages to try and smooth out the time it takes
 * to send an audit message
 */
public class AsyncAuditMessageRecorder implements IAuditMessageRecorder {
    
    private IAuditMessageRecorder delegate;
    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * @see org.bpmscript.audit.IAuditMessageRecorder#recordMessage(org.bpmscript.audit.IAuditMessage)
     */
    public void recordMessage(final IAuditMessage auditMessage) {
        executorService.submit(new Runnable() {
            public void run() {
                delegate.recordMessage(auditMessage);
            }
        });
    }

    public void setDelegate(IAuditMessageRecorder delegate) {
        this.delegate = delegate;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
