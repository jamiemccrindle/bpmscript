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

package org.bpmscript;

/**
 * Thrown if an assertion fails 
 */
public class AssertionFailureException extends BpmScriptException {

    private static final long serialVersionUID = 6734402319456995856L;

    /**
     * No arg constructor
     */
    public AssertionFailureException() {
        super();
    }

    /**
     * @param message why the assertion failed
     * @param cause a nested exception
     */
    public AssertionFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message why the assertion failed
     */
    public AssertionFailureException(String message) {
        super(message);
    }

    /**
     * @param cause a nested exception
     */
    public AssertionFailureException(Throwable cause) {
        super(cause);
    }

}
