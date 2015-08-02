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

package org.bpmscript.web;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

/**
 * Converts a Map of String to String arrays
 * 
 * so, some rules:
 * 
 * you can start with an array of the form [xxx] you can't have multidimentional arrays [x][y] you
 * can end with an array of the form [] the last element is always put on to the end of a
 * NativeObject rather than a NativeArray (where the last element can be an array or single value)
 */
public class ParamToJsConverter {

    public static final ParamToJsConverter DEFAULT_INSTANCE = new ParamToJsConverter();
    
    private static final Pattern p = Pattern.compile("^(.+?)\\[(\\d+)\\]$");

    private Object getValue(Scriptable scope, String name, Map<String, String[]> params) {
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return Context.javaToJS(values[0], scope);
        }
    }

    /**
     * 
     * @param params
     * @return
     */
    public Scriptable convert(Scriptable scope, Map<String, String[]> params) throws Exception {
        NativeObject result = new NativeObject();
        ScriptRuntime.setObjectProtoAndParent(result, scope);
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            Iterator<String> names = Arrays.asList(key.split("\\.")).iterator();
            convertObjectValue(result, names, params, key);
        }
        return result;
    }

    public void convertArrayValue(NativeArray scope, int index, Iterator<String> names, Map<String, String[]> params,
            String paramName) throws Exception {
        Object property = ScriptableObject.getProperty(scope, index);
        // it's a native object
        NativeObject nativeObject = null;
        if (property == UniqueTag.NOT_FOUND) {
            nativeObject = new NativeObject();
            ScriptRuntime.setObjectProtoAndParent(nativeObject, scope);
            ScriptableObject.putProperty(scope, index, nativeObject);
        } else if (property instanceof NativeObject) {
            nativeObject = (NativeObject) property;
        } else {
            throw new Exception(index + " should be a NativeObject but is already bound as " + property.getClass());
        }
        String name = names.next();
        if (names.hasNext()) {
            convertObjectValue(nativeObject, names, params, paramName);
        } else {
            convertLastValue(params, nativeObject, name, paramName);
        }
    }

    public void convertObjectValue(NativeObject scope, Iterator<String> names, Map<String, String[]> params,
            String paramName) throws Exception {
        String name = names.next();
        if (!names.hasNext()) {
            convertLastValue(params, scope, name, paramName);
        } else {
            Matcher matcher = p.matcher(name);
            if (matcher.find()) {
                // it's an array, it's parent is always an object
                String arraySubname = matcher.group(1);
                Integer index = Integer.parseInt(matcher.group(2));
                Object property = ScriptableObject.getProperty(scope, arraySubname);
                NativeArray nativeArray = null;
                if (property == UniqueTag.NOT_FOUND) {
                    nativeArray = new NativeArray(1);
                    ScriptRuntime.setObjectProtoAndParent(nativeArray, scope);
                    ScriptableObject.putProperty(scope, arraySubname, nativeArray);
                } else if (property instanceof NativeArray) {
                    nativeArray = (NativeArray) property;
                } else {
                    throw new Exception(arraySubname + " should be a NativeArray but is already bound as a "
                            + property.getClass() + ", " + "this means that " + arraySubname + " is bound both as "
                            + arraySubname + " without the []'s");
                }
                convertArrayValue(nativeArray, index, names, params, paramName);
            } else {
                // it's a native object
                Object property = ScriptableObject.getProperty(scope, name);
                NativeObject nativeObject = null;
                if (property == UniqueTag.NOT_FOUND) {
                    nativeObject = new NativeObject();
                    ScriptRuntime.setObjectProtoAndParent(nativeObject, scope);
                    ScriptableObject.putProperty(scope, name, nativeObject);
                } else if (property instanceof NativeObject) {
                    nativeObject = (NativeObject) property;
                } else {
                    throw new Exception(name + " should be a NativeObject but is already bound as "
                            + property.getClass() + " for param " + paramName);
                }
                convertObjectValue(nativeObject, names, params, paramName);
            }
        }
    }

    /**
     * @param current
     * @param subname
     */
    private void convertLastValue(Map<String, String[]> params, Scriptable scope, String subname, String paramName) {
        if (subname.endsWith("[]")) {
            // if it's an array
            String arraySubName = subname.substring(0, subname.length() - 2);
            ScriptableObject.putProperty(scope, arraySubName, NativeJavaArray.wrap(scope, params.get(paramName)));
        } else {
            // if it's a single value
            ScriptableObject.putProperty(scope, subname, getValue(scope, paramName, params));
        }
    }

}