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

import java.util.List;

import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;

/**
 * Provides query methods to get back information about messages that have been
 * recorded. 
 */
public interface IAuditMessageService {
    /**
     * @param messageId the message id of the message to get
     * @return the message if it exits, null otherwise
     */
    IAuditMessage getMessage(final String messageId);
    
    /**
     * @param query a paged query
     * @return a list of auditing messages
     */
    IPagedResult<IAuditMessage> findMessages(final IQuery query);
    
    /**
     * Find all the messages for a particular destination
     * 
     * @param query a paged query
     * @param destination the destination to filter messages on
     * @return the list of messages for that destination
     */
    IPagedResult<IAuditMessage> findMessagesByDestination(final IQuery query, final String destination);
    
    /**
     * @return a list of all the destinations used
     */
    List<String> findDestinations();
}