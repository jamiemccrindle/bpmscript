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

package org.bpmscript.language;

/**
 * 
 */
public class JavaBpmScriptTest {
    interface IMessageCallback {
        void handle(Object message);
        // void timeout();
    }

    interface IProcess {
        void start(Object message);
    }

    interface IChannel {
        void send(String address, Object message, IMessageCallback callback);

        void reply(Object request, Object response);

        Object getContent(Object message);
    }

    public void testJava() throws Exception {
        final IChannel channel = new IChannel() {
            public void send(String address, Object message, IMessageCallback callback) {
            }

            public void reply(Object request, Object response) {
            }

            public Object getContent(Object message) {
                return null;
            }
        };
        IProcess process = new IProcess() {
            int totalBanks = 0;
            int bankCount = 0;
            String bestBank = null;
            int bestRate = Integer.MAX_VALUE;
            public void start(final Object request) {
                channel.send("channel-credit-bureau", "ssn", new IMessageCallback() {
                    public void handle(Object message) {
                        channel.send("channel-lender-gateway", "asdf", new IMessageCallback() {
                            public void handle(Object message) {
                                final String[] banks = (String[]) channel.getContent(message);
                                for (final String bank : banks) {
                                    channel.send("channel-bank", message, new IMessageCallback() {
                                        String thisBank = bank;
                                        public void handle(Object message) {
                                            int rate = (Integer) channel.getContent(message);
                                            if (rate < bestRate) {
                                                bestRate = rate;
                                                bestBank = thisBank;
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        };
    }
}
