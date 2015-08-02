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

package org.bpmscript.integration.spring;

import org.springframework.integration.channel.MessageChannel;
import org.springframework.integration.message.Message;

/**
 * Adds and gets a serialisable returnAddress because the returnAddress is transient
 * for Spring Integration Messages.
 */
public class ReturnAddressSupport {
    
    private ISpringScriptMessageNames springScriptMessageNames = SpringScriptMessageNames.DEFAULT_INSTANCE;
    
    /**
     * @param message the message to set the header in
     * @return the message with the header set in it
     */
    public Message<?> setSerializeableReturnAddress(Message<?> message) {
        Object returnAddress = message.getHeader().getReturnAddress();
        if(returnAddress != null) {
            String returnAddressString = null;
            if(returnAddress instanceof MessageChannel) {
                MessageChannel returnChannel = (MessageChannel) returnAddress;
                returnAddressString = returnChannel.getName();
            } else {
                returnAddressString = (String) returnAddress;
            }
            message.getHeader().setAttribute(springScriptMessageNames.getSerializedReturnAddressName(), returnAddressString);
        }
        return message;
    }
    public Object getSerializeableReturnAddress(Message<?> message) {
        Object returnAddress = message.getHeader().getReturnAddress();
        if(returnAddress == null) {
            returnAddress = message.getHeader().getAttribute(springScriptMessageNames.getSerializedReturnAddressName());
        }
        return returnAddress;
    }
}
