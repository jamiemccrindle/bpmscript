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

import org.apache.commons.logging.LogFactory;
import org.bpmscript.*;
import org.bpmscript.channel.IScriptChannel;
import org.bpmscript.exec.*;
import org.bpmscript.exec.js.scope.IDefinitionValidator;
import org.bpmscript.exec.js.scope.ScriptValidator;
import org.bpmscript.js.DynamicContextFactory;
import org.bpmscript.process.IDefinitionConfiguration;
import org.mozilla.javascript.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The Javascript Process Executor exposes the sendFirst, sendNext and kill methods for sending messages to create
 * and then inform process instances respectively
 */
public class JavascriptProcessExecutor implements IProcessExecutor {

    protected final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private String processMethodName = "process";

    private IScopeService scopeService = null;

    private IContinuationService continuationService = null;
    
    private IDefinitionValidator definitionValidator = new ScriptValidator();

    private IJavascriptExecutor javascriptExecutor = new JavascriptExecutor();
    
    private String[] excludedNames;
    
    private IScriptChannel channel;

    /**
     * Create new JavascriptProcessExecutor
     */
    public JavascriptProcessExecutor() {

    }

    /**
     * Validate this process definition
     */
    public String validate(IProcessDefinition processDefinition)
            throws BpmScriptException {
        final Context cx = new DynamicContextFactory().enterContext();
        cx.setOptimizationLevel(-1);
        cx.setLanguageVersion(Context.VERSION_1_7);
        try {
            Map<String, Object> invocationContext = createInvocationContext(channel, processDefinition.getName());
            return definitionValidator.validate(processDefinition, invocationContext, cx);
        } finally {
            Context.exit();
        }
    }
    
    /**
     * Creates the invocation context by adding a __scriptChannel and a log object to a hashmap
     * 
     * The log is created with the definition name
     * 
     * @param channel the script channel to add to the context
     * @param definitionName the name of the definition
     * @return a map containing the scriptChannel and the log
     */
    public Map<String, Object> createInvocationContext(IScriptChannel channel, String definitionName) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("__scriptChannel", new ConvertingScriptChannel(channel));
        if(definitionName == null) {
            result.put("log", LogFactory
                    .getLog("NoDefinitionName"));
        } else {
            result.put("log", LogFactory
                    .getLog(definitionName));
        }
        return result;
    }

    /**
     * Sends the first message. This process executor checks the scope for a function that matches
     * the title case of the definitionName. It then hands that and all the request parameters to
     * a function called "process" (the processMethodName field). 
     * 
     * Once it has executed the definition, it stores the result
     */
    public IExecutorResult sendFirst(String processId, final String pid, final String branch, final String version,
            final IProcessDefinition processDefinition, final Object message,
            final String operation, final String parentVersion) throws BpmScriptException {
        final Context cx = new DynamicContextFactory().enterContext();
        cx.setOptimizationLevel(-1);
        cx.setLanguageVersion(Context.VERSION_1_7);
        try {
            Map<String, Object> invocationContext = createInvocationContext(channel, processDefinition.getName());
            final Scriptable scope = scopeService.createScope(processId, processDefinition, invocationContext, cx);

            // get the main function and execute it
            String constructorName = constructorName(processDefinition.getName());
            Object constructorObject = ScriptableObject.getProperty(scope, constructorName);
            Object processObject = ScriptableObject.getProperty(scope, processMethodName);

            if (constructorObject == null || !(constructorObject instanceof Function)) {
                throw new BpmScriptException("function name " + constructorName + " not found");
            }

            if (processObject == null || !(processObject instanceof Function)) {
                throw new BpmScriptException("process operation " + processMethodName + " not found");
            }

            final Function constructorFunction = (Function) constructorObject;
            final Function processFunction = (Function) processObject;

            IExecutorResult result = javascriptExecutor.execute(processDefinition.getName(), pid, branch, version, invocationContext, null, cx, scope,
                    new Callable<Object>() {
                        public Object call() throws Exception {
                            Object call = processFunction.call(cx, scope, scope, new Object[] { constructorFunction,
                                    processDefinition.getName(), operation, parentVersion, pid, branch, version,
                                    Context.javaToJS(message, scope) });
                            return call;
                        }
                    });
            IDefinitionConfiguration definitionConfiguration = channel.getDefinitionConfiguration(processDefinition.getName());
            HashMap<String, Object> serialisationContext = new HashMap<String, Object>();
            serialisationContext.putAll(definitionConfiguration.getProperties());
            serialisationContext.putAll(invocationContext);
            continuationService.storeResult(serialisationContext, excludedNames, result);
            return result;

        } catch (Throwable e) {
            log.warn(e, e);
            return new FailedResult(pid, branch, version, e, null);
        } finally {
            Context.exit();
        }

    }

    /**
     * @param definitionName the name to convert
     * @return the name with the first letter in upper case (title case)
     */
    private String constructorName(String definitionName) {
        String[] split = definitionName.split("\\.");
        String name = split[split.length - 1];
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Kills the process. Does this by restoring the continuation and passing it a "KILL" message. 
     * This gives the instance a chance to run any code required to release resources.
     */
    public IKilledResult kill(String definitionId, String definitionName, String pid, String branch, String version, String message) throws BpmScriptException {
        final Context cx = new DynamicContextFactory().enterContext();
        cx.setOptimizationLevel(-1);
        cx.setLanguageVersion(Context.VERSION_1_7);
        try {

            Map<String, Object> invocationContext = createInvocationContext(channel, definitionName);
            final Scriptable scope = scopeService.findScope(definitionId, invocationContext, cx);

            IDefinitionConfiguration definitionConfiguration = channel.getDefinitionConfiguration(definitionName);
            HashMap<String, Object> serialisationContext = new HashMap<String, Object>();
            serialisationContext.putAll(definitionConfiguration.getProperties());
            serialisationContext.putAll(invocationContext);
            NativeContinuation continuation = continuationService.getContinuation(serialisationContext, scope, pid, branch);
            final NativeContinuation unfrozen = continuation;
            final IKillMessage scriptMessage = new KillMessage(pid, branch, message);
            IExecutorResult result = javascriptExecutor.execute(definitionName, pid, branch, version, invocationContext, null, cx,
                    scope, new Callable<Object>() {
                        public Object call() throws Exception {
                            Object call = unfrozen.call(cx, scope, scope,
                                    new Object[] { Context.javaToJS(scriptMessage, scope) });
                            return call;
                        }
                    });
            continuationService.storeResult(serialisationContext, excludedNames, result);
            if (result instanceof IKilledResult) {
                IKilledResult killedResult = (IKilledResult) result;
                return killedResult;
            } else {
                throw new BpmScriptException("Process " + pid + " " + branch + " " + version + " not killed");
            }

        } catch (Throwable e) {
            throw new BpmScriptException(e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Sends in the next message. The right continuation is looked up using the pid and then executed.
     * The result is then stored.
     */
    public IExecutorResult sendNext(String definitionId, String definitionName, String pid, String branch, String version, String queueId, final Object message) throws BpmScriptException {

        final Context cx = new DynamicContextFactory().enterContext();
        cx.setOptimizationLevel(-1);
        cx.setLanguageVersion(Context.VERSION_1_7);
        try {

            Map<String, Object> invocationContext = createInvocationContext(channel, definitionName);
            final Scriptable scope = scopeService.findScope(definitionId, invocationContext, cx);

            IDefinitionConfiguration definitionConfiguration = channel.getDefinitionConfiguration(definitionName);
            HashMap<String, Object> serialisationContext = new HashMap<String, Object>();
            serialisationContext.putAll(definitionConfiguration.getProperties());
            serialisationContext.putAll(invocationContext);
            
            NativeContinuation continuation = continuationService.getContinuation(serialisationContext, scope, pid, branch);
            final NativeContinuation unfrozen = continuation;
            final INextMessage nextMessage = new NextMessage(pid, branch, version, queueId, message);
            IExecutorResult result = javascriptExecutor.execute(definitionName, pid, branch, version, invocationContext, null, cx,
                    scope, new Callable<Object>() {
                        public Object call() throws Exception {
                            Object call = unfrozen.call(cx, scope, scope,
                                    new Object[] { Context.javaToJS(nextMessage, scope) });
                            return call;
                        }
                    });
            continuationService.storeResult(serialisationContext, excludedNames, result);
            return result;

        } catch (Throwable e) {
            log.warn(e, e);
            return new FailedResult(pid, branch, version, e, null);
        } finally {
            Context.exit();
        }
    }

    /**
     * @param continuationService the service that stores and retrieves continuations
     */
    public void setContinuationService(IContinuationService continuationService) {
        this.continuationService = continuationService;
    }

    /**
     * @param processMethodName the name of the method to call to kick off  the process, defaults to "process"
     */
    public void setProcessMethodName(String processMethodName) {
        this.processMethodName = processMethodName;
    }

    /**
     * @param scopeService the scope service to use (this creates {@link Scriptable} scopes for a 
     * particular process)
     */
    public void setScopeService(IScopeService scopeService) {
        this.scopeService = scopeService;
    }

    /**
     * @param javascriptExecutor the javascriptexecutor responsible for converting the return from
     *   calling a process or restarting a continuation to an IExecutorResult
     */
    public void setScriptExecutor(IJavascriptExecutor javascriptExecutor) {
        this.javascriptExecutor = javascriptExecutor;
    }

    /**
     * @return the continuation service
     */
    public IContinuationService getContinuationService() {
        return continuationService;
    }

    /**
     * @param excludedNames add any names to explicitly exclude from serialisation
     */
    public void setExcludedNames(String[] excludedNames) {
        this.excludedNames = excludedNames;
    }

    /**
     * @param channel The script channel provider
     */
    public void setChannel(IScriptChannel channel) {
        this.channel = channel;
    }

}
