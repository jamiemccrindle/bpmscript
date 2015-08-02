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

package org.bpmscript.audit.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.bpmscript.audit.IAuditMessage;
import org.hibernate.annotations.Index;

/**
 * Persistent implementation of the audit message
 */
@Entity
@Table(name="AUDIT_MESSAGE")
public class HibernateAuditMessage implements IAuditMessage {
    
    private long id;
    private String messageId;
    private long timestampLong;
    private String destination;
    private String replyTo;
    private String correlationId;
    private String content;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    @Index(name = "IDX_AUD_MSGID")
    public String getMessageId() {
        return messageId;
    }
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    @Index(name = "IDX_AUD_TIMESTAMP")
    public long getTimestampLong() {
        return timestampLong;
    }
    public void setTimestampLong(long timestamp) {
        this.timestampLong = timestamp;
    }
    @Index(name = "IDX_AUD_DEST")
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    @Index(name = "IDX_AUD_REPLYTO")
    public String getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
    @Index(name = "IDX_AUD_CORRELATIONID")
    public String getCorrelationId() {
        return correlationId;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    @Lob
    @Column(length=262143)
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
