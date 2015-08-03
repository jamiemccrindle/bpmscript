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

package org.bpmscript.correlation.hibernate;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;


/**
 * A persistent class for storing correlations.
 */
@Entity
@Table(name = "CORRELATION", indexes = {
        @Index(columnList = "groupId"),
        @Index(columnList = "correlationId"),
        @Index(columnList = "expressions"),
        @Index(columnList = "valueshash")
})
public class HibernateCorrelation {

    private String id;
    private String channel;
    private String groupId;
    private String correlationId;
    private String expressions;
    private String valuesHash;
    private long timeout;
    private byte[] valueObjects;
    private byte[] replyToken;

    public HibernateCorrelation() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Column(length = 2047)
    public byte[] getReplyToken() {
        return replyToken;
    }

    @Lob
    @Column(length = 8191)
    public void setReplyToken(byte[] replyToken) {
        this.replyToken = replyToken;
    }

    public void setValueObjects(byte[] values) {
        this.valueObjects = values;
    }

    @Lob
    @Column(length = 8191)
    public byte[] getValueObjects() {
        return valueObjects;
    }

    @Column(unique = false, nullable = false)
    public String getExpressions() {
        return expressions;
    }

    public void setExpressions(String expressions) {
        this.expressions = expressions;
    }

    public String getValuesHash() {
        return valuesHash;
    }

    public void setValuesHash(String valuesHash) {
        this.valuesHash = valuesHash;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
