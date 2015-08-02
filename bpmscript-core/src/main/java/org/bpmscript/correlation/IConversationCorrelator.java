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
 * Correlates a conversation. The conversation concept is still one of correlation
 * but this could be the wrong package... This is a provider interface. Implementations
 * will typically take the in message and send it to the write conversation handler
 * that will join then join it to the appropriate conversation. 
 */
public interface IConversationCorrelator {
    
    /**
     * Synchronously pass a message into to a conversation identified by the 
     * toCorrelationId and wait for a response within timeout milliseconds.
     */
    Object call(String toCorrelationId, long timeout, Object in) throws Exception;
}
