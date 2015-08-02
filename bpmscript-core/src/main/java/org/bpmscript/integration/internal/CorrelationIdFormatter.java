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
package org.bpmscript.integration.internal;

/**
 * Turn a String into a CorrelationId and back again.
 */
public class CorrelationIdFormatter implements ICorrelationIdFormatter {
    
    public static final CorrelationIdFormatter DEFAULT_INSTANCE = new CorrelationIdFormatter();

    /**
     * Format a pid, branch, version and queueId into a string
     */
    public String format(String pid, String branch, String version, String queueId) {
        return pid + "/" + branch + "/" + version + "/" + queueId;
    }

    /**
     * Parse a correlationId as a string into an {@link ICorrelationId}
     */
    public ICorrelationId parse(String correlationId) {
        String[] split = correlationId.split("/");
        return new CorrelationId(split[0], split[1], split[2], split[3]);
    }
}
