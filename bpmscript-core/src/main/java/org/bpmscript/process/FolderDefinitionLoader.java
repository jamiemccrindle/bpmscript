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

package org.bpmscript.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bpmscript.BpmScriptException;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.util.FolderMonitor;
import org.bpmscript.util.StreamService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * Loads up definitions from a folder. Will also monitor the folder for changes.
 */
public class FolderDefinitionLoader implements InitializingBean, DisposableBean {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private IVersionedDefinitionManager versionedDefinitionManager;
    private BlockingQueue<ILibraryToFile> libraryChangeQueue;
    private IBpmScriptEngine engine;
    private IAutoStartManager autoStartManager;
    private Resource folder = null;
    private String suffix = ".js";
    private String definitionPrefix = "";
    private FolderMonitor folderMonitor = null;
    private boolean monitor = true;
    private boolean autoStartup = false;
    private boolean autoKill = false;
    
    private AtomicBoolean running = new AtomicBoolean(true);
    private ExecutorService libraryChangeService = Executors.newSingleThreadExecutor();

    public void setVersionedDefinitionManager(IVersionedDefinitionManager versionedDefinitionManager) {
        this.versionedDefinitionManager = versionedDefinitionManager;
    }

    public interface IFileChangeListener {
        public void onFileChanged(File file) throws Exception;
    }

    /**
     * Stop monitoring the folder for changes
     */
    public void destroy() throws Exception {
        running.set(false);
        folderMonitor.stop();
    }

    public void setDefinitionPrefix(String definitionPrefix) {
        this.definitionPrefix = definitionPrefix;
    }

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    public void setFolder(Resource folder) {
        this.folder = folder;
    }

    /**
     * Code to run when a library changes. 
     * 
     * @see org.bpmscript.js.reload.ILibraryChangeListener#onLibraryChange(java.lang.String,
     *      java.lang.String)
     */
    public void onLibraryChange(String library, String definitionName) {
        String rootPath;
        try {
            rootPath = folder.getFile().getCanonicalPath();
            String fileName = definitionName.replace(".", "/") + suffix;
            String filePath = rootPath + File.separator + fileName;
            String source = StreamService.DEFAULT_INSTANCE.readFully(new FileInputStream(filePath));
            IDefinition definition = versionedDefinitionManager.getPrimaryDefinition(definitionName);
            try {
                JavascriptProcessDefinition javascriptProcessDefinition = new JavascriptProcessDefinition(definitionName, source);
                String id = engine.validate(javascriptProcessDefinition);
                versionedDefinitionManager.createDefinition(id, javascriptProcessDefinition);
                versionedDefinitionManager.setDefinitionAsPrimary(id);
            } catch (BpmScriptException e) {
                log.error(e, e);
            }
            if (autoStartup) {
                autoStartManager.startup(definition.getName(), autoKill);
            }
        } catch (IOException e) {
            log.error(e, e);
        } catch (BpmScriptException e) {
            log.error(e, e);
        }
    }

    /**
     * @param engine the {@link IBpmScriptEngine} used for validation
     */
    public void setEngine(IBpmScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * Starts when the application context starts or is refreshed. Starts the folder monitor
     * and loads up any processes that the folder monitor finds. Also starts up the library change
     * monitor.
     */
    public void afterPropertiesSet() throws Exception {
        running.set(true);
        log.debug(folder);
        folderMonitor = new FolderMonitor(folder.getFile(), new IFileChangeListener() {

            public void onFileChanged(File file) throws Exception {
                String rootPath = folder.getFile().getCanonicalPath();
                String filePath = file.getCanonicalPath();
                String fileName = filePath.substring(rootPath.length() + 1);
                String definitionName = definitionPrefix
                        + fileName.substring(0, fileName.length() - suffix.length()).replace(
                                File.separator, ".");
                String source = StreamService.DEFAULT_INSTANCE.readFully(new FileInputStream(file));
                IJavascriptProcessDefinition currentDefinition = (IJavascriptProcessDefinition) versionedDefinitionManager.getPrimaryDefinition(definitionName);
                
                if (currentDefinition == null || !currentDefinition.getSource().equals(source)) {
                    JavascriptProcessDefinition javascriptProcessDefinition = new JavascriptProcessDefinition(definitionName, source);
                    String id = engine.validate(javascriptProcessDefinition);
                    versionedDefinitionManager.createDefinition(id, javascriptProcessDefinition);
                    versionedDefinitionManager.setDefinitionAsPrimary(id);
                } else {
                    log.debug(file + " timestamp has changed but contents are the same or we're starting up");
                }
                if (autoStartup) {
                    autoStartManager.startup(currentDefinition.getName(), autoKill);
                }
            }

        });
        folderMonitor.execute();
        if (monitor) {
            folderMonitor.start();
        }
        libraryChangeService.execute(new Runnable() {
            public void run() {
                while(running.get()) {
                    try {
                        ILibraryToFile libraryToFile = libraryChangeQueue.poll(1, TimeUnit.SECONDS);
                        if(libraryToFile != null) {
                            onLibraryChange(libraryToFile.getLibrary(), libraryToFile.getFile());
                        }
                    } catch (InterruptedException e) {
                        log.warn(e, e);
                    }
                }
            }
        });
    }

    public void setLibraryChangeQueue(BlockingQueue<ILibraryToFile> libraryChangeQueue) {
        this.libraryChangeQueue = libraryChangeQueue;
    }

    public void setAutoStartManager(IAutoStartManager autoStartManager) {
        this.autoStartManager = autoStartManager;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public void setAutoKill(boolean autoKill) {
        this.autoKill = autoKill;
    }

}
