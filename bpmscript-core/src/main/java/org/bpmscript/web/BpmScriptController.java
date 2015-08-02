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

package org.bpmscript.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bpmscript.InvalidQueueException;
import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.integration.internal.CorrelationIdFormatter;
import org.bpmscript.integration.internal.ICorrelationId;
import org.bpmscript.integration.internal.ICorrelationIdFormatter;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.process.IBpmScriptFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A Spring MVC Controller that either calls a definition or an instance if the 
 * request is part of a "Conversation". 
 * 
 * <h2>Calling a Definition</h2>
 * 
 * If no "conversationId" parameter is passed, the BpmScriptController extracts the Definition
 * Name and Operation from the URL and passes it into the message bus to run the definition
 * associated with the definition name.
 * 
 * <2>Calling an instance</h2>
 * 
 * If the "conversationId" parameter is set the controller knows that this call is part of a "conversation"
 * with an existing instance. A call is made to a {@link IConversationCorrelator} service which 
 * correlates the incoming request with the appropriate BpmScript instance.
 */
public class BpmScriptController extends AbstractController {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private IContinuationJournal continuationJournal;
    private ICorrelationIdFormatter correlationIdFormatter = CorrelationIdFormatter.DEFAULT_INSTANCE;
    private IBpmScriptFacade bpmScriptFacade;
    private IConversationCorrelator conversationCorrelator;
    private INameOperationStrategy nameOperationStrategy = new DefaultNameOperationStrategy();
    private long defaultTimeout = 10 * 1000;
    private String contentType = "text/html";

    @SuppressWarnings("unchecked")
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        response.setContentType(contentType);
        
        String correlationIdParam = request.getParameter("conversationId");
        String timeoutParam = request.getParameter("timeout");
        long timeout = defaultTimeout;
        if (timeoutParam != null) {
            try {
                timeout = Integer.parseInt(timeoutParam);
            } catch (NumberFormatException e) {
                log.debug(e);
            }
        }
        try {
            SerializableHttpServletRequest serializableHttpServletRequest = new SerializableHttpServletRequest(request);
            if (correlationIdParam == null) {
                INameOperation nameOperation = nameOperationStrategy.getNameOperation(request);
                Object result = null;
                String conversationId = null;
                Object message = bpmScriptFacade.call(nameOperation.getName(), nameOperation.getOperation(), timeout, serializableHttpServletRequest);
                if (message instanceof IInvocationMessage) {
                    IInvocationMessage conversationMessage = (IInvocationMessage) message;
                    result = conversationMessage.getArgs()[0];
                    conversationId = conversationMessage.getCorrelationId();
                } else {
                    result = message;
                }
                if (result instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) result;
                    if (conversationId != null) {
                        map.put("conversationId", conversationId);
                    }
                    String view = (String) map.get("view");
                    view = view.replace(".", "/");
                    ModelAndView modelAndView = new ModelAndView(view, map);
                    return modelAndView;
                } else {
                    throw new Exception("result must be a map or a conversation");
                }
            } else {

                IInvocationMessage conversationMessage = null;
                
                try {
                    conversationMessage = (IInvocationMessage) conversationCorrelator.call(
                            correlationIdParam, timeout, serializableHttpServletRequest);
                } catch(InvalidQueueException e) {
                    ICorrelationId parse = correlationIdFormatter.parse(correlationIdParam);
                    log.debug("creating branch for " + correlationIdParam);
                    String newBranch = continuationJournal.createBranch(parse.getVersion());
                    log.debug("sending again with new branch " + newBranch + " for " + correlationIdParam);
                    conversationMessage = (IInvocationMessage) conversationCorrelator.call(
                            correlationIdFormatter.format(parse.getPid(), newBranch, parse.getVersion(), parse.getQueueId()), timeout, serializableHttpServletRequest);
                }

                if(conversationMessage != null) {
                    Map<String, Object> result = (Map<String, Object>) conversationMessage.getArgs()[0];
                    result.put("conversationId", conversationMessage.getCorrelationId());
                    String view = (String) result.get("view");
                    view = view.replace(".", "/");
                    ModelAndView modelAndView = new ModelAndView(view, result);
                    return modelAndView;
                } else {
                    throw new Exception("Did not get a response for message " + correlationIdParam);
                }
            }
        } catch (Throwable e) {
            if (e instanceof Exception) {
                throw (Exception) e;
            } else {
                throw new Exception(e);
            }
        }
    }

    public void setBpmScriptFacade(IBpmScriptFacade bpmScriptFacade) {
        this.bpmScriptFacade = bpmScriptFacade;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void setConversationCorrelator(IConversationCorrelator conversationCorrelator) {
        this.conversationCorrelator = conversationCorrelator;
    }

    public void setContinuationJournal(IContinuationJournal continuationJournal) {
        this.continuationJournal = continuationJournal;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
