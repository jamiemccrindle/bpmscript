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
 * An AuditMessage value object
 */
public class AuditMessage implements IAuditMessage {
    
    private String messageId;
    private long timestampLong;
    private String destination;
    private String replyTo;
    private String correlationId;
    private String content;

    public AuditMessage(String content, String correlationId, String destination, String messageId, String replyTo,
            long timestamp) {
        super();
        this.content = content;
        this.correlationId = correlationId;
        this.destination = destination;
        this.messageId = messageId;
        this.replyTo = replyTo;
        this.timestampLong = timestamp;
    }

    /**
     * no args constructor 
     */
    public AuditMessage() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestampLong() {
        return timestampLong;
    }

    public void setTimestampLong(long timestamp) {
        this.timestampLong = timestamp;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
