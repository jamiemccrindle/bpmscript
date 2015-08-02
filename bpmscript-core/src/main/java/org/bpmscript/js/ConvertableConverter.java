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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.util.Collection;
import java.util.IdentityHashMap;
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
 * Converts objects
 */
// TODO: consider getting rid of this...
public class ConvertableConverter {
    
    @SuppressWarnings("unchecked")
    public Object convertObject(Scriptable scope, Object object, IdentityHashMap<Object, Object> alreadyConverted) {
        Object result = alreadyConverted.get(object);
        try {
            if(result != null) {
                return result;
            }
            if(object instanceof Map) {
                return convertMap(scope, (Map) object, alreadyConverted);
            } else if (object instanceof Collection) {
                return convertCollection(scope, (Collection) object, alreadyConverted);
            } else if (object instanceof Object[]) {
                return convertArray(scope, (Object[]) object, alreadyConverted);
            } else {
                Convertable annotation = object.getClass().getAnnotation(Convertable.class);
                if(annotation != null) {
                    
                    try {
                        BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
                        MethodDescriptor[] descriptors = beanInfo.getMethodDescriptors();
                    } catch (IntrospectionException e) {
                        throw new RuntimeException(e);
                    }
                    
                } else {
                    return Context.javaToJS(object, scope);
                }
            }
        } finally {
            if(result != null) {
                alreadyConverted.put(object, result);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object convertMap(Scriptable scope, Map map, IdentityHashMap<Object, Object> alreadyConverted) {
        NativeObject nativeObject = new NativeObject();
        ScriptRuntime.setObjectProtoAndParent(nativeObject, scope);
        Set entrySet = map.entrySet();
        for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ScriptableObject.putProperty(nativeObject, entry.getKey().toString(), convertObject(nativeObject, entry.getValue(), alreadyConverted));
        }
        return nativeObject;
    }
    
    @SuppressWarnings("unchecked")
    public Object convertCollection(Scriptable scope, Collection list, IdentityHashMap<Object, Object> alreadyConverted) {
        NativeArray nativeArray = new NativeArray(list.size());
        ScriptRuntime.setObjectProtoAndParent(nativeArray, scope);
        int i = 0;
        for (Object object : list) {
            ScriptableObject.putProperty(nativeArray, i++, convertObject(scope, object, alreadyConverted));
        }
        return nativeArray;
    }
    
    @SuppressWarnings("unchecked")
    public Object convertArray(Scriptable scope, Object[] array, IdentityHashMap<Object, Object> alreadyConverted) {
        NativeArray nativeArray = new NativeArray(array.length);
        ScriptRuntime.setObjectProtoAndParent(nativeArray, scope);
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            ScriptableObject.putProperty(nativeArray, i, convertObject(scope, object, alreadyConverted));
        }
        return nativeArray;
    }
    
}
