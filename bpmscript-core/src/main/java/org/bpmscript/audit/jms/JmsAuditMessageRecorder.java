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

import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.springframework.jms.core.JmsTemplate;

/**
 * Send audit messages to a JMS queue or topic.
 */
public class JmsAuditMessageRecorder implements IAuditMessageRecorder {

    private JmsTemplate template;
    
    /**
     * @see org.bpmscript.audit.IAuditMessageRecorder#recordMessage(org.bpmscript.audit.IAuditMessage)
     */
    public void recordMessage(IAuditMessage auditMessage) {
        template.convertAndSend(auditMessage);
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

}
