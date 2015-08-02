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
import java.io.OutputStream;

import org.bpmscript.exec.js.XMLStub;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableOutputStream;
import org.mozilla.javascript.xml.XMLObject;

/**
 * A stubbed output stream. Writes a Javascript object out replacing all stubbable objects with
 * stubs from the stub service.
 */
public class StubbedOutputStream extends ScriptableOutputStream {

    private final IStubService stubService;

    /**
     * Create a new Stubbed Output Stream with a downstream output stream, a scope for the
     * ScriptableOutputStream superclass and a stub service.
     * 
     * @param outputStream the output stream
     * @param scope the scope
     * @param stubService a stub service
     * @throws IOException if there is a problem writing to the downstream outputstream
     */
    public StubbedOutputStream(OutputStream outputStream, Scriptable scope, IStubService stubService)
            throws IOException {
        super(outputStream, scope);
        this.stubService = stubService;
    }

    /**
     * Replaces an object with it's stub if appropriate
     */
    @Override
    protected Object replaceObject(Object object) throws IOException {
        if (object == null)
            return null;
        Object result = internalReplaceObject(object);
        return result;
    }

    /**
     * Replaces an object with it's stub if appropriate using the stub service created in the
     * constructor
     */
    protected Object internalReplaceObject(Object object) throws IOException {
        if (object instanceof XMLObject) {
            XMLObject o = (XMLObject) object;
//            if(o.getClassName().equals("XML")) {
//                return new XMLStub((String) ScriptableObject.callMethod(o, "toXMLString", null));
//            }
            return o;
        } else {
            Stub stub = stubService.getStub(object);
            if (stub != null) {
                return stub;
            } else {
                return super.replaceObject(object);
            }
        }
    }
}