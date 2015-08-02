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

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * An implementation of {@link IResponseMessage} backed by a {@link Map}
 */
public class MapResponseMessage implements IResponseMessage, Serializable {

	/**
     * serial version id
     */
    private static final long serialVersionUID = -7020977428552127812L;
    
    private Map<String, Object> map;

	public MapResponseMessage(Map<String, Object> map) {
		this.map = map;
        this.map.put("messageId", UUID.randomUUID().toString());
	}

    public String getCorrelationId() {
        return (String) map.get("correlationId");
    }
    
    @Override
    public String toString() {
        return map != null ? map.toString() : "null message";
    }

    public Object getContent() {
        return map.get("content");
    }

    /**
     * @see org.bpmscript.integration.internal.IInternalMessage#getMessageId()
     */
    public String getMessageId() {
        return (String) map.get("messageId");
    }

    public void setMessageId(String messageId) {
        map.put("messageId", messageId);
    }

}
