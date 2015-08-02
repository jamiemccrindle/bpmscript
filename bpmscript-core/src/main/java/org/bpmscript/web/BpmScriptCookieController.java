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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.integration.internal.IInvocationMessage;
import org.bpmscript.process.IBpmScriptFacade;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that sets a cookie rather than relying on a correlationId set in the 
 * request params
 * 
 * TODO: we'll need to change it so that when a user comes back to the same page we show them
 * the page as it was rather than moving on to the next page...
 */
public class BpmScriptCookieController extends AbstractController {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private IBpmScriptFacade bpmScriptFacade;
    private IConversationCorrelator conversationCorrelator;
    private long defaultTimeout = 10 * 1000;
    private String defaultIndexName = "index";
    private String cookiePrefix = "BpmScript";
    private String contentType = "text/html";

    @SuppressWarnings("unchecked")
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType(contentType);

        String requestUri = request.getRequestURI();
        String definitionName = null;
        String methodName = null;
        String split[] = request.getRequestURI().split("/");
        if(requestUri.endsWith("/")) {
            definitionName = split[split.length - 1];
            methodName = defaultIndexName;
        } else {
            definitionName = split[split.length - 2];
            methodName = split[split.length - 1].split("\\.")[0];
        }
        
        String correlationIdParam = null;
        
        String cookieName = cookiePrefix + StringUtils.capitalize(definitionName) + StringUtils.capitalize(methodName);
        
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            if(cookieName.equals(name)) {
                correlationIdParam = cookie.getValue();
            }
        }

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
                Object result = null;
                String conversationId = null;
                Object message = bpmScriptFacade.call(definitionName, methodName, timeout, serializableHttpServletRequest);
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
                        response.addCookie(new Cookie(cookieName, conversationId));
                    }
                    ModelAndView modelAndView = new ModelAndView((String) map.get("view"), map);
                    return modelAndView;
                } else {
                    throw new Exception("result must be a map or a conversation");
                }
            } else {

                IInvocationMessage conversationMessage = null;
                
                conversationMessage = (IInvocationMessage) conversationCorrelator.call(
                        correlationIdParam, timeout, serializableHttpServletRequest);

                if(conversationMessage != null) {
                    Map<String, Object> result = (Map<String, Object>) conversationMessage.getArgs()[0];
                    String conversationId = conversationMessage.getCorrelationId();
                    result.put("conversationId", conversationId);
                    String replyTo = conversationMessage.getReplyTo();
                    Cookie cookie = new Cookie(cookieName, conversationId);
                    if(replyTo == null) {
                        cookie.setMaxAge(0);
                    }
                    response.addCookie(cookie);
                    ModelAndView modelAndView = new ModelAndView((String) result.get("view"), result);
                    return modelAndView;
                } else {
                    Cookie cookie = new Cookie(cookieName, "");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
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

    public void setDefaultIndexName(String defaultIndexName) {
        this.defaultIndexName = defaultIndexName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
