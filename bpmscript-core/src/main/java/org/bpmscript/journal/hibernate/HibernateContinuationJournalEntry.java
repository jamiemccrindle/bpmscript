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

package org.bpmscript.journal.hibernate;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.bpmscript.ProcessState;
import org.bpmscript.journal.IContinuationJournalEntry;
import org.hibernate.annotations.Index;

/**
 * Persistent class for storing continuation journal entries
 */
@Entity
@Table(name="CONTINUATION_JOURNAL")
public class HibernateContinuationJournalEntry implements IContinuationJournalEntry {
    
    private String branch;
    private Timestamp lastModified;
    private String state;
    private byte[] continuation;
    private String instanceId;
    private String version;

    private long id;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Version
    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    @Lob
    @Column(length=1048576)
    public byte[] getContinuation() {
        return continuation;
    }

    public void setContinuation(byte[] continuation) {
        this.continuation = continuation;
    }

    @Index(name = "IDX_CONT_PID")
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Index(name = "IDX_CONT_VERSION")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Index(name = "IDX_CONT_BRANCH")
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Index(name = "IDX_CONT_PROCESS_STATE")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Transient
    public String getPid() {
        return instanceId;
    }

    @Transient
    public ProcessState getProcessState() {
        return ProcessState.valueOf(state);
    }

}
