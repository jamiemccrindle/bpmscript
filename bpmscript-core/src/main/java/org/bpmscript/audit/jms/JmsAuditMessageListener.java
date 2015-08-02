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

package org.bpmscript.audit.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Listens for Audit messages from JMS and then records them. 
 */
public class JmsAuditMessageListener implements MessageListener {

    private MessageConverter converter;
    private IAuditMessageRecorder recorder;
    
    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message message) {
        try {
            recorder.recordMessage((IAuditMessage) converter.fromMessage(message));
        } catch (MessageConversionException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConverter(MessageConverter converter) {
        this.converter = converter;
    }

    public void setRecorder(IAuditMessageRecorder recorder) {
        this.recorder = recorder;
    }

}
