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

package org.bpmscript.exec.js;

import org.bpmscript.process.ScriptDefinition;

/**
 * A Javascript Process Definition. Contains a source string and reports its
 * type as "Javascript"
 */
public class JavascriptProcessDefinition extends ScriptDefinition implements IJavascriptProcessDefinition {

    private String source;

    /**
     * @param name the name of this definition
     * @param source the source for this definition
     */
    public JavascriptProcessDefinition(String name, String source) {
        super(name, IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT);
        this.source = source;
    }

    /**
     * @see org.bpmscript.exec.js.IJavascriptProcessDefinition#getSource()
     */
    public String getSource() {
        return source;
    }

}
