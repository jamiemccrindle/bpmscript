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

package org.bpmscript.exec.js.scope;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.js.Global;
import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.util.StreamService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * A script validator
 */
public class ScriptValidator implements IDefinitionValidator {
    
    /**
     * Validates that the Javascript can be executed. Javascript files should
     * only create Javascript functions.
     * 
     * @see IDefinitionValidator#validate(IProcessDefinition, Map, Context)
     */
    public String validate(IProcessDefinition processDefinition,
            Map<String, Object> invocationContext, Context cx) throws
            BpmScriptException {
        
        IJavascriptProcessDefinition javascriptProcessDefinition = (IJavascriptProcessDefinition) processDefinition;
        
        try {
            // create scripting environment (i.e. load everything)
            ScriptableObject scope = new Global(cx);

            Script script = cx.compileString(javascriptProcessDefinition.getSource(), processDefinition.getName(),
                    0, null);
            Stack<String> sourceStack = new Stack<String>();
            sourceStack.push(processDefinition.getName());
            cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
            LinkedBlockingQueue<ILibraryToFile> libraries = new LinkedBlockingQueue<ILibraryToFile>();
            cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraries);
            script.exec(cx, scope);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(processDefinition.getName().getBytes());
            md.update(javascriptProcessDefinition.getSource().getBytes());
            for (ILibraryToFile libraryToFile : libraries) {
                md.update(StreamService.DEFAULT_INSTANCE.readFully(this.getClass().getResourceAsStream(libraryToFile.getLibrary())).getBytes());
            }
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i=0;i<digest.length;i++) {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
            return hexString.toString();
        } catch (IOException e) {
            throw new BpmScriptException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new BpmScriptException(e);
        }
    }

}
