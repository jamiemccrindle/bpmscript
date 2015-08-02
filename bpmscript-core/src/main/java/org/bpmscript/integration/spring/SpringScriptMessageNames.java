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

/**
 * Script Message Names, this could just be an Enum. 
 */
public class SpringScriptMessageNames implements ISpringScriptMessageNames {

    public static final SpringScriptMessageNames DEFAULT_INSTANCE = new SpringScriptMessageNames();
    
    /**
     * @return "attributes"
     */
    public String getAttributesName() {
        return "attributes";
    }

    /**
     * @return "properties"
     */
    public String getPropertiesName() {
        return "properties";
    }

    /**
     * @return "payload"
     */
    public String getRequestContentName() {
        return "payload";
    }

    /**
     * @return "payload"
     */
    public String getResponseContentName() {
        return "payload";
    }

    /**
     * @return "serializedReturnAddress"
     */
    public String getSerializedReturnAddressName() {
        return "serializedReturnAddress";
    }

    /**
     * @return "address"
     */
    public String getAddressName() {
        return "address";
    }

    /**
     * @return "expiration"
     */
    public String getExpirationName() {
        return "expiration";
    }

    /**
     * @return "priority"
     */
    public String getPriorityName() {
        return "priority";
    }

    /**
     * @return "sequenceNumber"
     */
    public String getSequenceNumberName() {
        return "sequenceNumber";
    }

    /**
     * @return "sequenceSize"
     */
    public String getSequenceSizeName() {
        return "sequenceSize";
    }

}
