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

import java.util.Arrays;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Prints out JSON for a Javascript Object. Should really just use "toSource" on
 * the object
 */
// TODO: look at using toSource instead.
public class JSONPrinter {
    
    public static final JSONPrinter DEFAULT_INSTANCE = new JSONPrinter();
    
    private String lineSeparator = System.getProperty("line.separator");

    public String write(Object object) {
        return write(object, 0);
    }
    
    public String write(Object object, int depth) {
        if(object == null) {
            return "null";
        } if(object instanceof Scriptable) {
            return write((Scriptable) object, 0);
        } else if(object instanceof Object[]) {
            return Arrays.toString((Object[]) object);
        } else {
            return object.toString();
        }
    }
    
    public void indent(StringBuilder builder, int depth) {
        builder.append(lineSeparator);
        for(int i = 0; i < depth; i++) {
            builder.append(" ");
        }
    }
    
    public String write(Scriptable scriptable, int depth) {
        StringBuilder result = new StringBuilder();
        if(scriptable instanceof NativeJavaArray) {
            result.append("[");
            NativeJavaArray array = (NativeJavaArray) scriptable;
            Object[] objectArray = (Object[]) array.unwrap();
            for(int i = 0; i < objectArray.length; i++) {
                Object property = objectArray[i];
                if(property == null) {
                    result.append("null");
                } else if(property instanceof Scriptable) {
                    result.append(write((Scriptable) property, depth + 1));
                } else {
                    result.append("\"" + property.toString() + "\"");
                }
                if(i < objectArray.length - 1) {
                    result.append(",");
                }
            }
            result.append("]");
        } else if(scriptable instanceof NativeArray) {
            result.append("[");
            NativeArray array = (NativeArray) scriptable;
            long length = array.getLength();
            for(long i = 0; i < length; i++) {
                Object property = ScriptableObject.getProperty(array, (int) i);
                if(property == null) {
                    result.append("null");
                } else if(property instanceof Scriptable) {
                    result.append(write((Scriptable) property, depth + 1));
                } else {
                    result.append(property.toString());
                }
                if(i < length - 1) {
                    result.append(",");
                }
            }
            result.append("]");
        } else {
            result.append("{");
            Object[] ids = scriptable.getIds();
            for (int i = 0; i < ids.length; i++) {
                Object idObject = ids[i];
                if(idObject != null) {
                    Object object = ScriptableObject.getProperty(scriptable, idObject.toString());
                    if(object instanceof Scriptable) {
                        result.append("\"" + idObject.toString() + "\":" + write((Scriptable) object, depth + 1));
                    } else {
                        result.append("\"" + idObject.toString() + "\":" + object);
                    }
                }
                if(i < ids.length - 1) {
                    result.append(",");
                }
            }
            result.append("}");
        }
        return result.toString();
    }
}
