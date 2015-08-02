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
package org.bpmscript.integration.internal;

import java.util.Arrays;

/**
 * A value object invocation message
 */
public class InvocationMessage extends AbstractMessage implements IInvocationMessage {

    /**
     * serial version id
     */
    private static final long serialVersionUID = 3342166282998655746L;
    
    private String methodName;
    private String signature;
    private Object[] args;
    private String replyTo;

    public InvocationMessage() {
        super();
    }

    public InvocationMessage(String correlationId, String replyTo, Object... args) {
        super(correlationId);
        this.replyTo = replyTo;
        this.args = args;
    }

    @Override
    public String toString() {
        return "{messageId=" + getMessageId() + ", " + 
            "methodName=" + methodName + ", " +
            "replyTo=" + replyTo + ", " +
            "correlationId=" + getCorrelationId() + ", " +
            "args=" + Arrays.toString(args) + "}";
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object... args) {
        this.args = args;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

}
