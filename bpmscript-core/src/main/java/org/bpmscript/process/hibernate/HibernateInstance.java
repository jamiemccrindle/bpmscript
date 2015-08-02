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

package org.bpmscript.process.hibernate;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bpmscript.process.IInstance;
import org.bpmscript.process.TimeStampedIdentity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

/**
 * Persistent Instance
 */
@Entity
@Table(name="INSTANCE")
public class HibernateInstance implements IInstance, Serializable {
    
    private static final long serialVersionUID = -5245752084758265604L;

    private TimeStampedIdentity timeStampedIdentity = new TimeStampedIdentity();

    private String operation;
    private String parentVersion;
    private String definitionId;
    private String definitionName;
    private String definitionType;

    public Timestamp getCreationDate() {
        return timeStampedIdentity.getCreationDate();
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    public String getId() {
        return timeStampedIdentity.getId();
    }

    public void setCreationDate(Timestamp creationDate) {
        timeStampedIdentity.setCreationDate(creationDate);
    }

    public void setId(String id) {
        timeStampedIdentity.setId(id);
    }

    @Index(name="INDEX_INSTANCE_OPERATION")
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Index(name="INDEX_INSTANCE_PARENT_VERSION")
    public String getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    @Index(name="INDEX_INSTANCE_DEFINITION_ID")
    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }
}
