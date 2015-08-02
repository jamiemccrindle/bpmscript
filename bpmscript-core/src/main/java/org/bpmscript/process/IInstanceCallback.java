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

import org.bpmscript.IExecutorResult;

/**
 * Callback method used by the {@link IInstanceManager}.
 */
public interface IInstanceCallback {
    
    /**
     * @param processInstance the instance passed to the callback
     * @return any result of running this callback
     * @throws Exception if there was a problem running the callback
     */
	IExecutorResult execute(IInstance processInstance) throws Exception;
}