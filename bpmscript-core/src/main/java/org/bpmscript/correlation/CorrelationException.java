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

package org.bpmscript.correlation;

/**
 * General exception used by the correlation engine
 */
public class CorrelationException extends Exception {

    private static final long serialVersionUID = -1975592086460812722L;

    /**
     * Construct a new correlation exception
     */
    public CorrelationException() {
        super();
    }

    /**
     * Construct a new correlation exception
     * 
     * @param message the message
     * @param cause the underlying cause
     */
    public CorrelationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new correlation exception
     * 
     * @param message the message
     */
    public CorrelationException(String message) {
        super(message);
    }

    /**
     * Construct a new correlation exception
     * 
     * @param cause the underlying cause
     */
    public CorrelationException(Throwable cause) {
        super(cause);
    }
    
}
