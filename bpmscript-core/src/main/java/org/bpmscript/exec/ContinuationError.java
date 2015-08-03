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

import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeContinuation;

/**
 * An error that is thrown by Javascript processes that contains
 * a continuation so that the stack can be unwound and the continuation
 * can be restarted where it left off.
 * 
 * Also creates an EvaluatorException that contains Javascript 
 * Stack trace elements, useful for reporting where the continuation
 * stopped.
 */
public class ContinuationError extends Error {

	private static final long serialVersionUID = 7672770917586819930L;

	private NativeContinuation c;
	private EvaluatorException evaluatorException;
	
	/**
	 * Create a new error with the appropriate continuation 
	 * @param c the continuation. should not be null.
	 */
	public ContinuationError(NativeContinuation c) {
		this.evaluatorException = new EvaluatorException("continuation");
		this.c = c;
	}
	
	/**
	 * @return the continuation
	 */
	public NativeContinuation getContinuation() {
		return c;
	}
	
	/**
	 * @return the evaluator exception which contains the Javascript stacktrace
	 */
	public EvaluatorException getEvaluatorException() {
		return evaluatorException;
	}
}