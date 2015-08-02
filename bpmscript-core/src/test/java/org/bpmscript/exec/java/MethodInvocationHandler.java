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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;

/**
 * 
 */
public class MethodInvocationHandler implements IJavaMessageHandler<Object> {

    private Object process;
    private String methodName;
    private Object[] args;
    private AtomicBoolean hasResult = new AtomicBoolean(false);

    public MethodInvocationHandler(Object process, String methodName, Object... args) {
        this.process = process;
        this.methodName = methodName;
        this.args = args;
    }

    public ProcessState process(Object message) throws BpmScriptException {
        hasResult.set(true);
        Method foundMethod = getMethod(methodName);
        if (foundMethod != null) {
            try {
                Object[] arguments = new Object[args.length + 1];
                arguments[0] = message;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    arguments[i + 1] = arg;
                }
                Class<?> returnType = foundMethod.getReturnType();
                if(returnType.equals(Void.class)) {
                    foundMethod.invoke(process, arguments);
                    return ProcessState.PAUSED;
                }
                return (ProcessState) foundMethod.invoke(process, arguments);
            } catch (IllegalArgumentException e) {
                throw new BpmScriptException(e);
            } catch (IllegalAccessException e) {
                throw new BpmScriptException(e);
            } catch (InvocationTargetException e) {
                throw new BpmScriptException(e);
            }
        }
        return null;
    }

    /**
     * @return
     */
    private Method getMethod(String methodName) {
        Method[] methods = process.getClass().getMethods();
        Method foundMethod = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                foundMethod = method;
            }
        }
        return foundMethod;
    }

    public ProcessState timeout() throws BpmScriptException {
        if (!hasResult.get()) {
            Method foundMethod = getMethod(methodName + "Timeout");
            if (foundMethod != null) {
                try {
                    return (ProcessState) foundMethod.invoke(process, args);
                } catch (IllegalArgumentException e) {
                    throw new BpmScriptException(e);
                } catch (IllegalAccessException e) {
                    throw new BpmScriptException(e);
                } catch (InvocationTargetException e) {
                    throw new BpmScriptException(e);
                }
            } else {
                foundMethod = getMethod("timeout");
                if (foundMethod != null) {
                    try {
                        return (ProcessState) foundMethod.invoke(process, new Object[] {});
                    } catch (IllegalArgumentException e) {
                        throw new BpmScriptException(e);
                    } catch (IllegalAccessException e) {
                        throw new BpmScriptException(e);
                    } catch (InvocationTargetException e) {
                        throw new BpmScriptException(e);
                    }
                }
                // noop
                return ProcessState.PAUSED;
            }
        } else {
            return ProcessState.PAUSED;
        }
    }

}
