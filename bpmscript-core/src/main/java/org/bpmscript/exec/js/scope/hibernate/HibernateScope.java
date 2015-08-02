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

package org.bpmscript.exec.js.scope.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * Scope storage persistent class
 */
@Entity
@Table(name = "DEFINITION_SCOPE")
public class HibernateScope {

    private long id;
    private String definitionId;
    private byte[] scope;

    public HibernateScope() {
    }

    public HibernateScope(String definitionId, byte[] scope) {
        super();
        this.definitionId = definitionId;
        this.scope = scope;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Index(name = "IDX_SCOPE_DEFINITIONID")
    @Column(unique=true)
    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    @Lob
    @Column(length = 65534)
    public byte[] getScope() {
        return scope;
    }

    public void setScope(byte[] scope) {
        this.scope = scope;
    }

}
