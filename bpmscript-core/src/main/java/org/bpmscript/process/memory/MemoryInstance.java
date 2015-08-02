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
package org.bpmscript.process.memory;

import org.bpmscript.process.IInstance;
import org.bpmscript.process.TimeStampedIdentity;

/**
 * An in memory BpmScript instance
 */
public class MemoryInstance extends TimeStampedIdentity implements IInstance {
	
	private static final long serialVersionUID = 9058522982479824729L;

	private String operation;
	private String parentVersion;
	private String definitionId;
	private String definitionName;
	private String definitionType;
	
	public String getDefinitionId() {
		return definitionId;
	}
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
    public String getParentVersion() {
        return parentVersion;
    }
    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
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
