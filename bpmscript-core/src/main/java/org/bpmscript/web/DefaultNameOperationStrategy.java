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

import javax.servlet.http.HttpServletRequest;

/**
 * 
 */
public class DefaultNameOperationStrategy implements INameOperationStrategy {

    private String defaultDefinitionName = "home";
    private String defaultOperation = "index";
    private String prefix = "bpmscript/";
    
    /**
     * @see org.bpmscript.web.INameOperationStrategy#getNameOperation(javax.servlet.http.HttpServletRequest)
     */
    public INameOperation getNameOperation(HttpServletRequest request) {
        String[] split = request.getRequestURI().substring(request.getContextPath().length() + 1 + prefix.length()).split("/");
        StringBuilder definitionName = new StringBuilder();
        String operation = null;
        if(split.length == 0) {
            return new NameOperation(defaultDefinitionName, defaultOperation); 
        } else if (split.length == 1) {
            return new NameOperation(defaultDefinitionName, split[0].split("\\.")[0]); 
        } else {
            for(int i = 0; i < split.length; i++) {
                if(i == split.length - 1) {
                    operation = split[i].split("\\.")[0];
                } else {
                    definitionName.append(split[i]);
                    if(i < split.length - 2) {
                        definitionName.append(".");
                    }
                }
            }
        }
        return new NameOperation(definitionName.toString(), operation);
    }

    public void setDefaultDefinitionName(String defaultDefinitionName) {
        this.defaultDefinitionName = defaultDefinitionName;
    }

    public void setDefaultOperation(String defaultOperation) {
        this.defaultOperation = defaultOperation;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
