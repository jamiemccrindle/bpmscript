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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Gets a list of libraries and their association to files. Monitors those libraries
 * for changes and then publishes out all the files affected by the change. 
 */
public class LibraryFileMonitor implements InitializingBean, DisposableBean {

    private Timer timer = new Timer();
    private long pollMillis = 5000;
    private Map<String, Set<String>> libraryToFilesMap = new HashMap<String, Set<String>>();
    private HashMap<String, Long> libraryToLastModifiedMap = new HashMap<String, Long>();
    private BlockingQueue<ILibraryToFile> libraryChangeQueue = null;
    private BlockingQueue<ILibraryToFile> libraryAssociationQueue = null;
    
    /**
     * Kicks off the monitoring timer
     */
    public void afterPropertiesSet() throws Exception {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkLibraries();
            }
        }, 1000, pollMillis);
    }
    
    /**
     * Cancels the timer
     */
    public void destroy() throws Exception {
        timer.cancel();
    }

    /**
     * @param pollMillis how often the libraries should be checked for changes
     */
    public void setPollMillis(long pollMillis) {
        this.pollMillis = pollMillis;
    }

    /**
     * Checks whether the internal libraries have changed. If they do, publishes
     * out to a queue the list of files that need to be reloaded as a result
     */
    @SuppressWarnings("unchecked")
    protected void checkLibraries() {
        ArrayList<ILibraryToFile> newLibraryToFiles = new ArrayList<ILibraryToFile>();
        libraryAssociationQueue.drainTo(newLibraryToFiles);
        for (ILibraryToFile libraryToFile : newLibraryToFiles) {
            Set set = libraryToFilesMap.get(libraryToFile.getLibrary());
            if(set == null) {
                set = new HashSet<String>();
                libraryToFilesMap.put(libraryToFile.getLibrary(), set);
            }
            set.add(libraryToFile.getFile());
        }
        Iterator iterator = libraryToFilesMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Entry) iterator.next();
            String library = (String) entry.getKey();
            URL resource = this.getClass().getResource(library);
            if("file".equals(resource.getProtocol())) {
                String path = resource.getPath();
                File file = new File(path);
                Long newLastModified = file.lastModified();
                Long oldLastModified = libraryToLastModifiedMap.get(library);
                if(oldLastModified != null && !(oldLastModified.equals(newLastModified))) {
                    // library has changed, we should go through its files and notify listeners
                    // that they need to reload. also, we need to check to see if the files are
                    // libraries themselves...
                    Collection values = (Collection) entry.getValue();
                    for (Iterator valueIterator = values.iterator(); valueIterator.hasNext();) {
                        String value = (String) valueIterator.next();
                        if(libraryToFilesMap.containsKey(value)) {
                            // TODO: here we need to recurse
                        } else {
                            // notify listeners that the file has changed. consider notifying
                            // listeners that the library has changed...
                            libraryChangeQueue.add(new LibraryToFile(library, value));
                        }
                    }
                    libraryToLastModifiedMap.put(library, newLastModified);
                } else if (oldLastModified == null) {
                    libraryToLastModifiedMap.put(library, newLastModified);
                }
            }
        }
    }

    public void setLibraryChangeQueue(BlockingQueue<ILibraryToFile> libraryChangeQueue) {
        this.libraryChangeQueue = libraryChangeQueue;
    }

    public void setLibraryAssociationQueue(BlockingQueue<ILibraryToFile> libraryAssociationQueue) {
        this.libraryAssociationQueue = libraryAssociationQueue;
    }


    
}
