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

package org.bpmscript.correlation;

import java.io.Serializable;
import java.util.List;

/**
 * A correlation service. A correlation associates a number of rules (correlations) 
 * that are run against incoming messages (received through the "send" method). 
 * If the message matches the correlation rules, it is passed to the entity that 
 * registered the correlation.
 * 
 * Correlations
 * 
 * Correlations are a set of "and" and "equals" expressions. The left side of the 
 * "equals" evaluates some part of the message, the right hand side much be a 
 * literal object. "or" expressions are provided by registering multiple correlations.
 * 
 * Expression Language
 * 
 * The expressions are just registered as Strings and could be implemented in any
 * dynamic language. The left hand side should look something like "message.getInvoiceId()"
 * and the left side could then be "23423423". 
 * 
 * Channel
 * 
 * Channels are named sources of messages. A correlation is registered against a channel
 * and incoming messages are sent to a channel. There are two reasons for this: first,
 * it allows an optimisation that only correlations within a channel need to be evaluated
 * for messages to that channel. Secondly, data that is passed to a channel SHOULD be 
 * of the same type.
 * 
 * GroupId
 * 
 * The group id provides a way to group correlations so that they can be removed in a
 * group. For BPM or Workflow software this is often the instance id of the process so
 * that when it dies / completes, it can remove all the correlations associated with it.
 * 
 * CorrelationId
 * 
 * The correlationId identifies a specific correlation. Also used for removal of correlations.
 * NOTE: this is NOT the correlation id that is used in messaging.
 * 
 * ReplyToken
 * 
 * When a messages matches a correlation, it is sent to the listener registered for that
 * correlation using the ReplyToken. How this happens is typically managed by an adapter
 * to a particular messaging system. The reply token will typically consist of a returnAddress
 * and a messaging correlation id but not necessarily (e.g. it could be a message exchange)
 * 
 * Timeouts
 * 
 * Depending on the implementation it is likely that the more correlations registered for 
 * a particular channel will eventually either mean that there are memory issues if it is in memory
 * or speed issues running all of the correlation criteria. The timeout ensures that
 * old entries are not run.
 */
public interface ICorrelationService {

    /**
     * Add a correlation. 
     * 
     * @param channel the channel that messages to correlate on will come in on. must not be null.
     * @param groupId the group id used to group correlations. useful for deletion by group later.
     * @param correlationId the correlationId for this correlation. used for deleting the correlation.
     * @param replyToken the token that is passed downstream to ensure that a matched 
     *   message goes to the right place.
     * @param key the correlation key, consists of a set of lhs eq rhs expressions
     * @param timeout the length of time that this correlation should be active for in milliseconds
     * @throws CorrelationException if there is a problem adding this correlation
     */
    void addCorrelation(String channel, String groupId, String correlationId, Serializable replyToken, ICorrelation key, long timeout) throws CorrelationException;

    /**
     * Remove a correlation by correlation id
     * 
     * @param correlationId the correlation id that was used to register the correlation
     */
    void removeCorrelation(String correlationId);

    /**
     * Send a message to the correlation service. This part of the interface is not typically used
     * by the entities registering correlations but rather by the entities sending messages that
     * may be matched by correlations 
     * 
     * @param channelName the channel name. must not be null.
     * @param message the message that may be matched by the correlations
     * @throws CorrelationException if there was a problem matching against this message
     */
    List<Object> send(String channelName, Object message) throws CorrelationException;

    /**
     * Remove all the correlations associated with this group id
     * 
     * @param groupId the group id that the correlations were registered with
     */
    void removeAllCorrelations(String groupId);

}