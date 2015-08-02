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

import java.io.IOException;
import java.io.InputStream;

import org.bpmscript.exec.js.XMLStub;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.xml.XMLLib;

/**
 * A Stubbed Input Stream. Extends ScriptableInputStream and add support for
 * swapping stubbed objects in the stream with their equivalents from the top
 * level scope.
 */
public class StubbedInputStream extends ScriptableInputStream {
	
	private final IStubService stubService;
    private Scriptable scope;
	
	/**
	 * Create a new Stubbed Input Stream with the top level scope to use to 
	 * restore stubs and a stub service used for looking up the stubs
	 * 
	 * @param inputStream the downstream inputstream
	 * @param scope the top level scope
	 * @param stubService the stub service
	 * @throws IOException if there is a problem reading from the stream.
	 */
	public StubbedInputStream(InputStream inputStream, Scriptable scope, IStubService stubService) throws IOException {
		super(inputStream, scope);
		this.scope = scope;
		this.stubService = stubService;
	}
	
	/**
	 * Resolve an object (if it is a stub, replace it)
	 */
	@Override
	protected Object resolveObject(Object object) throws IOException {
	    if(object instanceof XMLStub) {
	        XMLLib xmlLib = XMLLib.extractFromScope(scope);
	        
	    }
		if(object instanceof Stub) {
			Object realObject = stubService.getObject((Stub) object);
			return realObject;
		} else {
			return super.resolveObject(object);
		}
	}
}