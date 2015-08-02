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

package org.bpmscript.audit.memory;

import java.util.LinkedList;
import java.util.List;

import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.bpmscript.audit.IAuditMessageService;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;

/**
 * In MemoryBpmScriptEngine implementation of the audit services
 */
public class MemoryAuditService implements IAuditMessageRecorder, IAuditMessageService {

    private LinkedList<IAuditMessage> messages = new LinkedList<IAuditMessage>();
    private long maxMessages;

    /**
     * @see org.bpmscript.audit.IAuditMessageRecorder#recordMessage(org.bpmscript.audit.IAuditMessage)
     */
    public synchronized void recordMessage(IAuditMessage auditMessage) {
        while(messages.size() >= maxMessages) {
            messages.removeLast();
        }
        messages.addFirst(auditMessage);
    }

    /**
     * @see org.bpmscript.audit.IAuditMessageService#findMessages(org.bpmscript.paging.IQuery)
     */
    public IPagedResult<IAuditMessage> findMessages(IQuery query) {
        return null;
    }

    /**
     * @see org.bpmscript.audit.IAuditMessageService#getMessage(java.lang.String)
     */
    public IAuditMessage getMessage(String messageId) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.bpmscript.audit.IAuditMessageService#findDestinations()
     */
    public List<String> findDestinations() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.bpmscript.audit.IAuditMessageService#findMessagesByDestination(org.bpmscript.paging.IQuery, java.lang.String)
     */
    public IPagedResult<IAuditMessage> findMessagesByDestination(IQuery query, String destination) {
        // TODO Auto-generated method stub
        return null;
    }

}
