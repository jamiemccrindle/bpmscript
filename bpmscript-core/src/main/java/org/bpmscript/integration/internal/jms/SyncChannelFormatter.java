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

/**
 * Implementation of the {@link ISyncChannelFormatter} for formatting addresses
 * for synchronous messaging (i.e. messaging to ensure that a message comes back
 * to the container that the call originated from).
 */
public class SyncChannelFormatter implements ISyncChannelFormatter {
    
    private String remoteAddress = null;
    private String prefix = "__sync";
    private String separator = ":";

    public SyncChannelFormatter() {
        super();
    }

    public SyncChannelFormatter(String remoteAddress) {
        super();
        this.remoteAddress = remoteAddress;
    }

    public String format(String address) {
        return prefix + separator + remoteAddress + separator + address;
    }

    public boolean isSync(String address) {
        return address.startsWith(prefix);
    }

    public ILocalAndRemoteAddress parse(String address) {
        String[] split = address.split(separator);
        return new LocalAndRemoteAddress(split[1], split[2]);
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

}
