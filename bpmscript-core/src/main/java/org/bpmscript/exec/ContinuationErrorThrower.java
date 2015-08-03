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

import org.mozilla.javascript.NativeContinuation;

import java.io.Serializable;

/**
 * Throws continuation and kill exceptions
 */
public class ContinuationErrorThrower implements Serializable {
    
    private static final long serialVersionUID = 1602776531369234779L;
    
    /**
     * @param continuation throw a new continuation error
     */
    public void throwContinuation(NativeContinuation continuation) {
        throw new ContinuationError(continuation);
    }
    
    /**
     * @param message exits with a killed message
     */
    public void throwKill(String message) {
        throw new KillError(message);
    }
}