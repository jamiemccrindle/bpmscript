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

import java.util.HashMap;
import java.util.Map;

import org.bpmscript.channel.IConverter;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Transforms Javascript {@link Scriptable} objects to maps
 * and back again.
 */
public class JsConverter implements IConverter {

	protected Object internalConvert(Object in) {
		Object result = in;
		if (in instanceof NativeArray) {
			result = convertArray((NativeArray) in);
        } else if (in instanceof ScriptableObject) {
            result = convertObject((ScriptableObject) in);
        } else if (in instanceof NativeJavaObject) {
            result = ((NativeJavaObject) in).unwrap();
        }
		return result;
	}

	protected Object[] convertArray(NativeArray in) {
		int length = (int) in.getLength();
		Object[] result = new Object[length];
		for(int i = 0; i < length; i++) {
			result[i] = internalConvert(in.get(i, in));
		}
		return result;
	}

	protected Map<String, Object> convertObject(ScriptableObject scriptableObject) {
		Object[] ids = scriptableObject.getIds();
		Map<String, Object> mapResult = new HashMap<String, Object>();
		for (Object id : ids) {
			String idString = id.toString();
			mapResult.put(idString, internalConvert(scriptableObject.get(idString, scriptableObject)));
		}
		return mapResult;
	}

	public Object convert(Object in) {
		return internalConvert(in);
	}

}
