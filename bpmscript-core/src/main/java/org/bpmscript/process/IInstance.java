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
 * Information about a running process instance. This includes denormalised information
 * from the process definition.
 */
public interface IInstance extends IIdentity {
    
    /**
     * @return the id for the definition that this instance was created from
     */
	String getDefinitionId();
	
	/**
	 * @return name of the definition that this instance was created from
	 */
	String getDefinitionName();
	
	/**
	 * @return the type of definition that this instance was created from e.g. java or javascript
	 */
	String getDefinitionType();
	
	/**
	 * @return the operation that was called on the definition to produce this instance
	 */
	String getOperation();
	
	/**
	 * @return the version of the parent instance that resulted in this instance being
	 *   created. null if there was no parent instance.
	 */
	String getParentVersion();
}
