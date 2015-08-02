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

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.Factory;
import com.opensymphony.module.sitemesh.HTMLPage;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;

/**
 * A Template controller. Allows Spring templates to drive Sitemesh.
 * 
 * @author jmccrindle
 */
public class TemplateController extends ParameterizableViewController {

    private Map<String,Object> templateModel = new HashMap<String,Object>();
    
    /**
     * Gets the sitemesh HTMLPage and passes that information to the right view
     * along with the following parameters:
     * 
     * <ul>
     * <li>base - the context path</li>
     * <li>locale - the locale for the page</li>
     * <li>req - the http request</li>
     * <li>res - the http response</li>
     * </ul>
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        HTMLPage htmlPage = (HTMLPage) request.getAttribute(RequestConstants.PAGE);

        Map<String,Object> context = new HashMap<String,Object>(templateModel);
        
        context.put("base", request.getContextPath());
        context.put("locale", request.getLocale());

        // For backwards compatability with apps that used the old VelocityDecoratorServlet
        // that extended VelocityServlet instead of VelocityViewServlet
        context.put("req", request);
        context.put("res", response);

        if (htmlPage == null) {
            context.put("title", "Title?");
            context.put("body", "<p>Body?</p>");
            context.put("head", "<!-- head -->");
        }
        else {
            context.put("title", OutputConverter.convert(htmlPage.getTitle()));
            {
                StringWriter buffer = new StringWriter();
                htmlPage.writeBody(OutputConverter.getWriter(buffer));
                context.put("body", buffer.toString());
            }
            {
                StringWriter buffer = new StringWriter();
                htmlPage.writeHead(OutputConverter.getWriter(buffer));
                context.put("head", buffer.toString());
            }
            context.put("page", htmlPage);
            Factory factory = Factory.getInstance(new Config(new ServletConfig() {
                public ServletContext getServletContext() {
                    return TemplateController.this.getServletContext();
                }

                public String getInitParameter(String name) {return null;}
                public String getServletName() {return null;}
                public Enumeration<?> getInitParameterNames() {return null;}
            }));
            Decorator decorator = factory.getDecoratorMapper().getDecorator(request, htmlPage);
            context.put("decorator", decorator);
        }
        return new ModelAndView(getViewName(), context);        
    }

    public void setTemplateModel(Map<String,Object> templateModel) {
        this.templateModel = templateModel;
    }
}
