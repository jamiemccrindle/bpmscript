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

package org.bpmscript.process;

/**
 * A value object that can be used by a messaging system to call the BpmScript
 * engine.
 */
public class BpmScriptRequest implements IBpmScriptRequest {

    private String parentVersion;
    private String definitionName;
    private String operation;
    private Object message;
    
    public BpmScriptRequest() {
    }
    
    public BpmScriptRequest(String parentVersion, String definitionName, String operation, Object message) {
        super();
        this.parentVersion = parentVersion;
        this.definitionName = definitionName;
        this.operation = operation;
        this.message = message;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.process.IBpmScriptRequest#getParentVersion()
     */
    public String getParentVersion() {
        return parentVersion;
    }
    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.process.IBpmScriptRequest#getDefinitionName()
     */
    public String getDefinitionName() {
        return definitionName;
    }
    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.process.IBpmScriptRequest#getOperation()
     */
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.process.IBpmScriptRequest#getMessage()
     */
    public Object getMessage() {
        return message;
    }
    public void setMessage(Object message) {
        this.message = message;
    }

}
