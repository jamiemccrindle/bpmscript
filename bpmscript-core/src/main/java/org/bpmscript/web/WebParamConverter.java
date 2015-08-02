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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * customer.address[0].xxx = "20b" = { customer: { address[0]: { xxx: "20b" } address[1]: {  } } }
 * customer.address[0].yyy = "20b" = { customer: { address: [ { xxx: "20b", yyy: "20b"} ] } }
 * customer.address[1].houseNumber = "20b" { customer: { address: [ { xxx: "20b", yyy: "20b" } {
 * houseNumber: "20b" } ] } } customer.address.billing.houseNumber = "20b" customer.interest[]=
 * "hockey","fighting" customer.gender = "Male" customer.dob = "14/5/2008" customer.pin = "1234"
 * customer[0].address[0].xxx = "20b" customer[0][1].blah
 */
public class WebParamConverter {

    private static final Pattern p = Pattern.compile("^(.+?)\\[(\\d+)\\]$");

    private String getValue(String name, Map<String, String[]> params) {
        String[] values = params.get(name);
        if (values == null || values.length == 0) {
            return null;
        } else {
            return values[0];
        }
    }

    /**
     * 
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(Map<String, String[]> params) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Set<String> keySet = params.keySet();
        for (String name : keySet) {
            String[] names = name.split("\\.");
            Map current = result;
            for (int i = 0; i < names.length; i++) {
                String subname = names[i];
                if (i == names.length - 1) {
                    lastElement(params, current, name, subname);
                    break;
                } else {
                    Map next = (Map) current.get(subname);
                    if(next == null) {
                        next = new HashMap();
                        current.put(subname, next);
                    }
                    current = next;
                }
            }
        }
        convertArrays(result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void convertArrays(Map<String, Object> root) throws Exception {
        Set<String> keySet = new HashSet<String>(root.keySet());
        for (String key : keySet) {
            Object value = root.get(key);
            if(value instanceof Map) {
                convertArrays((Map<String, Object>) value);
                Matcher matcher = p.matcher(key);
                if(matcher.find()) {
                    String subname = matcher.group(1);
                    Integer index = Integer.parseInt(matcher.group(2));
                    Object object = root.get(subname);
                    if(object == null) {
                        object = new ArrayList();
                        root.put(subname, object);
                    }
                    if(object instanceof List) {
                        List list = (List) object;
                        if(list.size() < index + 1) {
                            for(int i = list.size(); i < index + 1; i++) {
                                list.add(null);
                            }
                        }
                        list.set(index, root.get(key));
                    } else {
                        throw new Exception(subname + " is already bound");
                    }
                    root.remove(key);
                }
            }
        }
    }
    

    /**
     * @param current
     * @param subname
     */
    @SuppressWarnings("unchecked")
    private void lastElement(Map<String, String[]> params, Map map, String name, String subname) {
        // if it's an array
        if (subname.endsWith("[]")) {
            String arraySubName = subname.substring(0, subname.length() - 2);
            map.put(arraySubName, params.get(name));
        } else
        // if it's a single value
        {
            map.put(subname, getValue(name, params));
        }
    }

}