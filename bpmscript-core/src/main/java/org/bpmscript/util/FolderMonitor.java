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

package org.bpmscript.util;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bpmscript.process.FolderDefinitionLoader.IFileChangeListener;

/**
 * Monitors a folder for changed files
 */
// TODO: very slow memory leak if files are deleted
public class FolderMonitor {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(this.getClass());

    private File folder;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicBoolean running = new AtomicBoolean(true);
    private IFileChangeListener listener;
    private long pollMillis = 5000;
    private boolean recurse = true;
    private HashMap<String, Long> cachedFiles = new HashMap<String, Long>();

    public FolderMonitor() {
        super();
    }

    /**
     * @param folder the folder to monitor
     * @param listener the listener to callback on file change or file found
     */
    public FolderMonitor(File folder, IFileChangeListener listener) {
        this.folder = folder;
        this.listener = listener;
    }

    /**
     * Starts monitoring the folder for changed files.
     * @throws Exception if the folder cannot be monitored e.g. if it not a folder
     */
    public void start() throws Exception {
        if (!folder.isDirectory()) {
            throw new Exception(folder + " is not a directory");
        }
        executorService.execute(new Runnable() {
            public void run() {
                while (running.get()) {
                    try {
                        execute();
                        Thread.sleep(pollMillis);
                    } catch (Exception e) {
                        log.warn(e, e);
                    }
                }
            }
        });
    }

    /**
     * Stop monitoring the folder
     */
    public void stop() {
        running.set(false);
    }

    /**
     * @param folder the folder to monitor if it wasn't set in the constructor
     */
    public void setFolder(File folder) {
        this.folder = folder;
    }

    /**
     * @param listener the listener to call back when files are initially found or
     *   change.
     */
    public void setListener(IFileChangeListener listener) {
        this.listener = listener;
    }

    /**
     * @param pollMillis how many milliseconds to wait between checking the files
     *   in the folder that is being monitored.
     */
    public void setPollMillis(long pollMillis) {
        this.pollMillis = pollMillis;
    }

    /**
     * Execute the file monitor
     */
    public void execute() throws Exception {
        synchronized (cachedFiles) {
            recurse(folder);
        }
    }

    /**
     * Recursively go through the files and check for changes.
     */
    private void recurse(File directory) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    Long lastModified = cachedFiles.get(file.getAbsolutePath());
                    if (lastModified == null || !lastModified.equals(file.lastModified())) {
                        try {
                            listener.onFileChanged(file);
                            cachedFiles.put(file.getAbsolutePath(), file.lastModified());
                        } catch (Exception ex) {
                            log.warn(ex, ex);
                        }
                    }
                } else {
                    if (recurse) {
                        recurse(file);
                    }
                }
            }
        }
    }
}