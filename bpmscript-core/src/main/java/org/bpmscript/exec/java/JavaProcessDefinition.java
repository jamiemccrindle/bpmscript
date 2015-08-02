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

package org.bpmscript.exec.java;

import org.bpmscript.process.ScriptDefinition;

/**
 * @see IJavaProcessDefinition 
 */
public class JavaProcessDefinition extends ScriptDefinition implements IJavaProcessDefinition {

    /**
     * Create a new JavaProcessDefinition
     * 
     * @param name the name of this definition
     * @param url the url to the definition. how the url is interpreted 
     *   depends on the JavaProcessExecutor
     * @see JavaProcessExecutor
     */
    public JavaProcessDefinition(String name, String url) {
        super(name, DEFINITION_TYPE_JAVA);
        this.url = url;
    }

    private String url;

    /**
     * @see org.bpmscript.exec.java.IJavaProcessDefinition#getUrl()
     */
    public String getUrl() {
        return url;
    }

}
