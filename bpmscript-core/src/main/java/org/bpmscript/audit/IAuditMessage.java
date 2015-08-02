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

/**
 * An interface for messages that have been recorded
 */
public interface IAuditMessage {
    
    /**
     * @return a message id for this message
     */
    String getMessageId();
    
    /**
     * @return when this message was sent
     */
    long getTimestampLong();
    
    /**
     * @return the destination that this message was bound for
     */
    String getDestination();
    
    /**
     * @return where response messages should go. could be null.
     */
    String getReplyTo();
    
    /**
     * @return a correlation id
     */
    String getCorrelationId();
    
    /**
     * @return the contents of the message
     */
    String getContent();
}
