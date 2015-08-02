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

package org.bpmscript.js.reload;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;

/**
 * 
 */
public class LibraryChangeNotifier implements InitializingBean {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private List<ILibraryChangeListener> listeners;
    private ExecutorService libraryChangeService = Executors.newSingleThreadExecutor();
    private BlockingQueue<ILibraryToFile> libraryChangeQueue;
    private AtomicBoolean running = new AtomicBoolean(true);

    public List<ILibraryChangeListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<ILibraryChangeListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * Start listening for change and publishing out the results
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        libraryChangeService.execute(new Runnable() {
            public void run() {
                while(running.get()) {
                    try {
                        ILibraryToFile libraryToFile = getLibraryChangeQueue().poll(1, TimeUnit.SECONDS);
                        if(libraryToFile != null) {
                            for (ILibraryChangeListener listener : listeners) {
                                listener.onLibraryChange(libraryToFile.getLibrary(), libraryToFile.getFile());
                            }
                        }
                    } catch (InterruptedException e) {
                        log.warn(e, e);
                    }
                }
            }
        });
    }
    
    /**
     * Stop the checking for changed files
     */
    public void destroy() throws Exception {
        running.set(false);
    }

    /**
     * @param libraryChangeQueue the libraryChangeQueue to set
     */
    public void setLibraryChangeQueue(BlockingQueue<ILibraryToFile> libraryChangeQueue) {
        this.libraryChangeQueue = libraryChangeQueue;
    }

    /**
     * @return the libraryChangeQueue
     */
    public BlockingQueue<ILibraryToFile> getLibraryChangeQueue() {
        return libraryChangeQueue;
    }
}
