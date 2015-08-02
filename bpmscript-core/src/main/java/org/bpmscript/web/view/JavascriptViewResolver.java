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

import java.util.concurrent.BlockingQueue;

import org.bpmscript.js.IJavascriptSourceCache;
import org.bpmscript.js.MapToJsConverter;
import org.bpmscript.js.reload.ILibraryChangeListener;
import org.bpmscript.js.reload.ILibraryToFile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * A Javascript View Resolver
 */
public class JavascriptViewResolver extends AbstractTemplateViewResolver implements InitializingBean, ILibraryChangeListener {

    private IJavascriptSourceCache javascriptSourceCache;
    private String configResource = "classpath:/org/bpmscript/web/config.js";
    private MapToJsConverter mapToJsConverter = MapToJsConverter.DEFAULT_INSTANCE;
    private BlockingQueue<ILibraryToFile> libraryAssociationQueue; 
    
    /**
     * Clears the view cache when a library changes.
     */
    public void onLibraryChange(String library, String file) {
        super.clearCache();
    }
    
    /**
     * Sets default viewClass to <code>requiredViewClass</code>.
     * 
     * @see #setViewClass
     * @see #requiredViewClass
     */
    public JavascriptViewResolver() {
        setViewClass(requiredViewClass());
    }
    
    /**
     * Returns a new {@link JavascriptView}
     */
    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
       JavascriptView view = (JavascriptView) super.buildView(viewName);
       view.setJavascriptSourceCache(javascriptSourceCache);
       view.setConfigResource(configResource);
       view.setMapToJsConverter(mapToJsConverter);
       view.setLibraryAssociationQueue(libraryAssociationQueue);
       return view;
    }

    /**
     * Requires JavascriptView.
     * 
     * @see JavascriptView
     */
    protected Class<?> requiredViewClass() {
        return JavascriptView.class;
    }

    public void setJavascriptSourceCache(IJavascriptSourceCache javascriptSourceCache) {
        this.javascriptSourceCache = javascriptSourceCache;
    }

    public void setConfigResource(String configResource) {
        this.configResource = configResource;
    }

    public void setMapToJsConverter(MapToJsConverter mapToJsConverter) {
        this.mapToJsConverter = mapToJsConverter;
    }

    public void setLibraryAssociationQueue(BlockingQueue<ILibraryToFile> libraryAssociationQueue) {
        this.libraryAssociationQueue = libraryAssociationQueue;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(libraryAssociationQueue, "libraryAssociationQueue must not be null");
    }

}
