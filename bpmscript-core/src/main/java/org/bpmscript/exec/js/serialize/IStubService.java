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

/**
 * Implementations will return an object for a stub and a stub for an
 * Object. The implementation should use object identity for the Object
 * lookup.
 */
public interface IStubService {
    
    /**
     * @param stub the stub to look up
     * @return the object associated with the stub
     */
	Object getObject(Stub stub);
	
	/**
	 * @param object the object to look up
	 * @return the stub associated with the object
	 */
	Stub getStub(Object object);
}