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

import junit.framework.TestCase;

/**
 * 
 */
public class DefaultNameOperationStrategyTest extends TestCase {

    /**
     * Test method for {@link org.bpmscript.web.DefaultNameOperationStrategy#getNameOperation(javax.servlet.http.HttpServletRequest)}.
     */
    public void testGetNameOperationSub() {
        DefaultNameOperationStrategy strategy = new DefaultNameOperationStrategy();
        INameOperation nameOperation = strategy.getNameOperation(createRequest("/bpmscript", "/bpmscript/bpmscript/cleanband/home/index.html"));
        assertEquals("cleanband.home", nameOperation.getName());
        assertEquals("index", nameOperation.getOperation());
    }

    /**
     * Test method for {@link org.bpmscript.web.DefaultNameOperationStrategy#getNameOperation(javax.servlet.http.HttpServletRequest)}.
     */
    public void testGetNameOperationDefault() {
        DefaultNameOperationStrategy strategy = new DefaultNameOperationStrategy();
        INameOperation nameOperation = strategy.getNameOperation(createRequest("/bpmscript", "/bpmscript/bpmscript/index.html"));
        assertEquals("home", nameOperation.getName());
        assertEquals("index", nameOperation.getOperation());
    }

    /**
     * Test method for {@link org.bpmscript.web.DefaultNameOperationStrategy#getNameOperation(javax.servlet.http.HttpServletRequest)}.
     */
    public void testGetNameOperationRoot() {
        DefaultNameOperationStrategy strategy = new DefaultNameOperationStrategy();
        INameOperation nameOperation = strategy.getNameOperation(createRequest("", "/bpmscript/cleanband/home/index.html"));
        assertEquals("cleanband.home", nameOperation.getName());
        assertEquals("index", nameOperation.getOperation());
    }


    /**
     * @param contextPath
     * @param requestURI
     * @return
     */
    private MutableHttpServletRequest createRequest(String contextPath, String requestURI) {
        MutableHttpServletRequest request = new MutableHttpServletRequest();
        request.setContextPath(contextPath);
        request.setRequestURI(requestURI);
        return request;
    }

}
