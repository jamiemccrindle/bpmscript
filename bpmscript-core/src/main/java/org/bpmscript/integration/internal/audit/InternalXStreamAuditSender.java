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

package org.bpmscript.integration.internal.audit;

import org.bpmscript.audit.AuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.integration.internal.IMessageSender;

import com.thoughtworks.xstream.XStream;

/**
 * Adapter class that converts {@link IInternalMessage}'s and then sends
 * them on to an {@link IAuditMessageRecorder}.
 */
public class InternalXStreamAuditSender implements IMessageSender {

    private XStream xstream = new XStream();
    private IAuditMessageRecorder recorder;
    
    /**
     * @see org.bpmscript.integration.internal.IMessageSender#send(java.lang.String, org.bpmscript.integration.internal.IInternalMessage)
     */
    public void send(String address, IInternalMessage internalMessage) {
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.setMessageId(internalMessage.getMessageId());
        auditMessage.setCorrelationId(internalMessage.getCorrelationId());
        auditMessage.setDestination(address);
        auditMessage.setTimestampLong(System.currentTimeMillis());
        String xml = xstream.toXML(internalMessage);
        auditMessage.setContent(xml);

        if (internalMessage instanceof IInvocationMessage) {
            IInvocationMessage invocationMessage = (IInvocationMessage) internalMessage;
            String replyTo = invocationMessage.getReplyTo();
            auditMessage.setReplyTo(replyTo);
        }
        
        recorder.recordMessage(auditMessage);
    }

    public void setRecorder(IAuditMessageRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * @param xstream the xstream to set
     */
    public void setXStream(XStream xstream) {
        this.xstream = xstream;
    }

}
