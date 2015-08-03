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

package org.bpmscript.web.view;

import org.bpmscript.js.Global;
import org.bpmscript.js.IJavascriptSourceCache;
import org.bpmscript.js.MapToJsConverter;
import org.bpmscript.js.reload.ILibraryToFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.beans.BeansException;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

/**
 * A Javascript View
 */
public class JavascriptView extends AbstractTemplateView {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private IJavascriptSourceCache javascriptSourceCache;
    private String configResource;
    private MapToJsConverter mapToJsConverter;
    private BlockingQueue<ILibraryToFile> libraryAssociationQueue; 

    public JavascriptView() {
        setExposeSpringMacroHelpers(false);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void renderMergedTemplateModel(Map model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Locale locale = RequestContextUtils.getLocale(request);
        Context cx = new ContextFactory().enterContext();
        try {
            // create scripting environment (i.e. load everything)
            ScriptableObject scope = new Global(cx);
            cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraryAssociationQueue);
            ScriptableObject.putProperty(scope, "log", Context.javaToJS(log, scope));
            ScriptableObject.putProperty(scope, "request", Context.javaToJS(request, scope));
            ScriptableObject.putProperty(scope, "response", Context.javaToJS(response, scope));
            ScriptableObject.putProperty(scope, "base", Context.javaToJS(request.getContextPath(), scope));
            ScriptableObject.putProperty(scope, "locale", Context.javaToJS(locale, scope));
            Set<Map.Entry> entrySet = model.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                ScriptableObject.putProperty(scope, entry.getKey(), mapToJsConverter.convertObject(scope, entry.getValue()));
            }
            Stack<String> sourceStack = new Stack<String>();
            sourceStack.push(configResource);
            cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
            if(libraryAssociationQueue != null) {
                cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraryAssociationQueue);
            }
            Script configScript = javascriptSourceCache.getScript(
                    new String(
                            StreamUtils.copyToByteArray(
                                    getApplicationContext().getResource(configResource).getInputStream())),
                    configResource);
            configScript.exec(cx, scope);
            sourceStack.pop();
            
            sourceStack.push(getUrl());
            Script script = javascriptSourceCache.getScript(
                    new String(
                            StreamUtils.copyToByteArray(
                                    getApplicationContext().getResource(getUrl()).getInputStream())),
                    getUrl());
            Object result = script.exec(cx, scope);
            response.getWriter().write(result.toString());
            // not sure if this is necessary
            response.getWriter().flush();
        } finally {
            Context.exit();
        }
    }
    
    private String encoding;

    /**
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Return the encoding for the FreeMarker template.
     */
    protected String getEncoding() {
        return this.encoding;
    }

    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
    }

    /**
     * Expose helpers unique to each rendering operation. This is necessary so that
     * different rendering operations can't overwrite each other's formats etc.
     * <p>Called by <code>renderMergedTemplateModel</code>. The default implementation
     * is empty. This method can be overridden to add custom helpers to the model.
     * @param model The model that will be passed to the template at merge time
     * @param request current HTTP request
     * @throws Exception if there's a fatal error while we're adding information to the context
     * @see #renderMergedTemplateModel
     */
    protected void exposeHelpers(Map<?,?> model, HttpServletRequest request) throws Exception {
    }

    public void setMapToJsConverter(MapToJsConverter mapToJsConverter) {
        this.mapToJsConverter = mapToJsConverter;
    }

    public void setJavascriptSourceCache(IJavascriptSourceCache javascriptSourceCache) {
        this.javascriptSourceCache = javascriptSourceCache;
    }

    public void setConfigResource(String configResource) {
        this.configResource = configResource;
    }

    public void setLibraryAssociationQueue(BlockingQueue<ILibraryToFile> libraryAssociationQueue) {
        this.libraryAssociationQueue = libraryAssociationQueue;
    }

}
