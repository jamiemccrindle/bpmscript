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

import org.bpmscript.ICompletedResult;
import org.bpmscript.ProcessState;

/**
 * Represents a completed result
 */
public class CompletedResult extends ExecutorResult implements ICompletedResult {

	private Object result = null;
	
	public CompletedResult(String pid, String branch, String version, Object result) {
		super(pid, branch, version, ProcessState.COMPLETED);
		this.result = result;
	}

	/**
	 * @see org.bpmscript.ICompletedResult#getResult()
	 */
	public Object getResult() {
		return result;
	}

}
