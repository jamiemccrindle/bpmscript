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

package org.bpmscript.integration.internal.jms;

import java.io.Serializable;

import org.bpmscript.integration.internal.IInternalMessage;

/**
 * Address combined with internalMessage value object
 */
public class AddressAndMessage implements Serializable {
    
    /**
     * serial version id 
     */
    private static final long serialVersionUID = 2225621597445676507L;
    
    private final String address;
    private final IInternalMessage internalMessage;

    public AddressAndMessage(String address, IInternalMessage internalMessage) {
        super();
        this.address = address;
        this.internalMessage = internalMessage;
    }

    public String getAddress() {
        return address;
    }

    public IInternalMessage getMessage() {
        return internalMessage;
    }
}
