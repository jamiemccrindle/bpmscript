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
package org.bpmscript.exec;

import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;

/**
 * Abstract Executor Result. Convenience class for IExecutorResult
 * subclasses.
 */
public abstract class ExecutorResult implements IExecutorResult {

	private ProcessState processState;
	private String pid;
	private String branch;
	private String version;

	/**
	 * Create a new Executor Result with the appropriate pid, branch
	 * version and process state
	 */
	public ExecutorResult(String pid, String branch, String version, ProcessState processState) {
		super();
		this.pid = pid;
		this.branch = branch;
		this.version = version;
		this.processState = processState;
	}
	
	public ProcessState getProcessState() {
		return processState;
	}

	public String getPid() {
		return pid;
	}

    public String getVersion() {
        return version;
    }

    public String getBranch() {
        return branch;
    }


}
