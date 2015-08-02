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

package org.bpmscript.exec.js.serialize;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebuggableScript;

/**
 * The Function Stub Service goes through a scope finding all top level functions
 * and their prototypes and creates stubs for them. This is used in the serialisation
 * of a continuation, stubbing out expensive to serialise functions and replacing
 * when the continuation is deserialised.
 */
public class FunctionStubService {

    /**
     * Get the stubs for scope
     * 
     * @param scope the scope to check
     * @return a set of stubs generated from the scope
     */
	Stubs getStubs(Scriptable scope) {
		Map<Stub, Object> stubsToObjects = new HashMap<Stub, Object>();
		Map<Object, Stub> objectsToStub = new IdentityHashMap<Object, Stub>();
		Stubs stubs = new Stubs(stubsToObjects, objectsToStub);
		addStubs("", scope, stubs);
		return stubs;
	}
	
	/**
	 * Adds stubs. This method is called recursively. The prefix is added to
	 * as the depth increases to keep the stub names unique.
	 * 
	 * @param prefix the prefix to use
	 * @param scope the scope to check
	 * @param stubs the list of stubs to add to
	 */
	private void addStubs(String prefix, Scriptable scope, Stubs stubs) {
		Object[] ids = scope.getIds();
		for (Object id : ids) {
			String idString = id.toString();
			Object value = scope.get(idString, scope);
			if(value instanceof NativeFunction) {
				NativeFunction function = ((NativeFunction) value);
				addStubs(prefix + idString + ".", function.getDebuggableView(), stubs.stubsToObject, stubs.objectsToStub);
				Scriptable prototype = (Scriptable) function.get("prototype", function);
				addStubs(prefix + idString + ".prototype.", prototype, stubs);
			}
		}
	}

	/**
     * Adds stubs. This method is called recursively. The prefix is added to
     * as the depth increases to keep the stub names unique.
     *
	 * @param prefix the stub prefix
	 * @param script a debuggable script
	 * @param stubsToObjects the stubsToObjects map
	 * @param objectsToStub the objectsToStubs map
	 */
	private void addStubs(String prefix, DebuggableScript script, Map<Stub, Object> stubsToObjects, Map<Object, Stub> objectsToStub) {
		FunctionStub functionStub = new FunctionStub(prefix);
		stubsToObjects.put(functionStub, script);
		objectsToStub.put(script, functionStub);
		int functionCount = script.getFunctionCount();
		for(int i = 0; i < functionCount; i++) {
			DebuggableScript function = script.getFunction(i);
			addStubs(prefix + i + ".", function, stubsToObjects, objectsToStub);
		}
	}
}