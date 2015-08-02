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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bpmscript.util.StreamService;

/**
 * Serializable HTTP Servlet Request
 */
@SuppressWarnings("unchecked")
public class SerializableHttpServletRequest implements HttpServletRequest, Serializable {

    /**
     * Serial version id 
     */
    private static final long serialVersionUID = 2746641581031466567L;
    
    private String authType;
    private String contextPath;
    private SerializableCookie[] cookies;
    private String method;
    private String pathInfo;
    private String pathTranslated;
    private String queryString;
    private String remoteUser;
    private String requestURI;
    private StringBuffer requestURL;
    private String requestedSessionId;
    private String servletPath;
    private boolean requestedSessionIdFromCookie;
    private boolean requestedSessionIdFromURL;
    private boolean requestedSessionIdValid;
    private Hashtable<String, Object> attributes;
    private String characterEncoding;
    private String contentType;
    private String data;
    private String localAddr;
    private String localName;
    private int localPort;
    private Vector<Locale> locales;
    private Hashtable<String, String[]> parameters;
    private String protocol;
    private String remoteAddr;
    private String remoteHost;
    private int remotePort;
    private String scheme;
    private String serverName;
    private int serverPort;
    private boolean secure;
    private Hashtable<String, Vector<String>> headers;

    public SerializableHttpServletRequest(Map<String, String[]> params) {
        this.parameters = new Hashtable<String, String[]>(params);
    }

    public SerializableHttpServletRequest(HttpServletRequest request) {
        authType = request.getAuthType();
        contextPath = request.getContextPath();
        Cookie[] requestCookies = request.getCookies();
        if(requestCookies != null) {
            cookies = new SerializableCookie[requestCookies.length];
            for (int i = 0; i < requestCookies.length; i++) {
                Cookie cookie = requestCookies[i];
                cookies[i] = new SerializableCookie(cookie);
            }
        }
        method = request.getMethod();
        pathInfo = request.getPathInfo();
        pathTranslated = request.getPathTranslated();
        queryString = request.getQueryString();
        remoteUser = request.getRemoteUser();
        requestURI = request.getRequestURI();
        requestURL = request.getRequestURL();
        requestedSessionId = request.getRequestedSessionId();
        servletPath = request.getServletPath();
        requestedSessionIdFromCookie = request.isRequestedSessionIdFromCookie();
        requestedSessionIdFromURL = request.isRequestedSessionIdFromURL();
        requestedSessionIdValid = request.isRequestedSessionIdValid();
        Enumeration attributeNames = request.getAttributeNames();
        attributes = new Hashtable<String, Object>();
        while (attributeNames.hasMoreElements()) {
            String name = (String) attributeNames.nextElement();
            Object attributeValue = request.getAttribute(name);
            if(attributeValue instanceof Serializable) {
                attributes.put(name, attributeValue);
            }
        }
        characterEncoding = request.getCharacterEncoding();
        contentType = request.getContentType();
        try {
            data = StreamService.DEFAULT_INSTANCE.readFully(request.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        localAddr = request.getLocalAddr();
        localName = request.getLocalName();
        localPort = request.getLocalPort();
        Enumeration requestLocales = request.getLocales();
        locales = new Vector<Locale>();
        while (requestLocales.hasMoreElements()) {
            Locale locale = (Locale) requestLocales.nextElement();
            locales.add(locale);
        }
        parameters = new Hashtable<String, String[]>(request.getParameterMap());
        protocol = request.getProtocol();
        remoteAddr = request.getRemoteAddr();
        remoteHost = request.getRemoteHost();
        remotePort = request.getRemotePort();
        scheme = request.getScheme();
        serverName = request.getServerName();
        serverPort = request.getServerPort();
        secure = request.isSecure();
        headers = new Hashtable<String, Vector<String>>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            Enumeration headerValues = request.getHeaders(name);
            Vector<String> values = new Vector<String>();
            while (headerValues.hasMoreElements()) {
                String value = (String) headerValues.nextElement();
                values.add(value);
            }
            headers.put(name, values);
        }
    }

    public SerializableHttpServletRequest() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return authType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return contextPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        Cookie[] result = new Cookie[cookies.length];
        for (int i = 0; i < cookies.length; i++) {
            SerializableCookie cookie = cookies[i];
            result[i] = cookie.getCookie();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String name) {
        String header = getHeader(name);
        try {
            return new SimpleDateFormat().parse(header).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        Vector<String> headerValues = headers.get(name);
        if (headerValues != null && headerValues.size() > 0) {
            return headerValues.get(0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return headers.keys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        Vector<String> headerValues = headers.get(name);
        if (headerValues != null) {
            return headerValues.elements();
        } else {
            return new Vector().elements();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String name) {
        int result = Integer.parseInt(getHeader(name));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    public String getMethod() {
        return this.method;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return this.pathInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        return this.pathTranslated;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return this.queryString;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return this.remoteUser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return this.requestURI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return this.requestURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        return this.servletPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        throw new UnsupportedOperationException("getSession not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException("getSession not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("getUserPrincipal not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        return requestedSessionIdFromURL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException("isUserInRole not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        return contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream response = new ByteArrayInputStream(data == null ? new byte[] {} : data.getBytes());
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return response.read();
            }

            @Override
            public void close() throws IOException {
                response.close();
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr() {
        return localAddr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName() {
        return localName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort() {
        return localPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        if (locales != null && locales.size() > 0) {
            return locales.get(0);
        } else {
            return Locale.getDefault();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        if (locales != null && locales.size() > 0) {
            return locales.elements();
        } else {
            Vector<Locale> vector = new Vector<Locale>();
            vector.add(Locale.getDefault());
            return vector.elements();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        String[] params = parameters.get(name);
        if (params != null && params.length > 0) {
            return params[0];
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return parameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return protocol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data == null ? new byte[] {} : data.getBytes())));
        return reader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        throw new UnsupportedOperationException("getRealPath not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort() {
        return remotePort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("getRequestDispatcher not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return scheme;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        return serverName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return serverPort;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        return secure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
