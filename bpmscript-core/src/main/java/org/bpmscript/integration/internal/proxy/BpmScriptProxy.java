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
package org.bpmscript.integration.internal.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.bpmscript.process.IBpmScriptFacade;

/**
 * This class allows a bpmscript to assume an interface and look like a 
 * regular service. Useful for things like spring
 * 
 * @author jamie
 */
public class BpmScriptProxy implements InvocationHandler {
    
	private String definitionName;
	private long defaultTimeout;
	private Map<String, Long> timeouts;
	private IBpmScriptFacade bpmScriptFacade;

	public BpmScriptProxy(IBpmScriptFacade bpmScriptFacade,
			String definitionName, long timeout, Map<String, Long> timeouts) {
		super();
		this.bpmScriptFacade = bpmScriptFacade;
		this.definitionName = definitionName;
		this.defaultTimeout = timeout;
		this.timeouts = timeouts;
	}

	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
        long timeout = defaultTimeout;
        Long specificTimeout = null;
        if(timeouts != null) {
            specificTimeout = timeouts.get(method.getName());
        }
        if(specificTimeout != null) {
            timeout = specificTimeout;
        }
	    return bpmScriptFacade.call(definitionName, method.getName(), timeout, args);
	}
}
