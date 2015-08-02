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

package org.bpmscript.js;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Converts a Map to a Javascript Object... It might be interesting to look
 * at what would happen if we used JSON lib instead...
 */
public class MapToJsConverter {
    
    public static final MapToJsConverter DEFAULT_INSTANCE = new MapToJsConverter();

    @SuppressWarnings("unchecked")
    public Object convertObject(Scriptable scope, Object object) {
        if(object instanceof Map) {
            return convertMap(scope, (Map) object);
        } else if (object instanceof Collection) {
            return convertCollection(scope, (Collection) object);
        } else if (object instanceof Object[]) {
            return convertArray(scope, (Object[]) object);
        } else {
            return Context.javaToJS(object, scope);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Object convertMap(Scriptable scope, Map map) {
        NativeObject nativeObject = new NativeObject();
        ScriptRuntime.setObjectProtoAndParent(nativeObject, scope);
        Set entrySet = map.entrySet();
        for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ScriptableObject.putProperty(nativeObject, entry.getKey().toString(), convertObject(nativeObject, entry.getValue()));
        }
        return nativeObject;
    }
    
    @SuppressWarnings("unchecked")
    public Object convertCollection(Scriptable scope, Collection list) {
        NativeArray nativeArray = new NativeArray(list.size());
        ScriptRuntime.setObjectProtoAndParent(nativeArray, scope);
        int i = 0;
        for (Object object : list) {
            ScriptableObject.putProperty(nativeArray, i++, convertObject(scope, object));
        }
        return nativeArray;
    }
    
    @SuppressWarnings("unchecked")
    public Object convertArray(Scriptable scope, Object[] array) {
        NativeArray nativeArray = new NativeArray(array.length);
        ScriptRuntime.setObjectProtoAndParent(nativeArray, scope);
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            ScriptableObject.putProperty(nativeArray, i, convertObject(scope, object));
        }
        return nativeArray;
    }
    
}