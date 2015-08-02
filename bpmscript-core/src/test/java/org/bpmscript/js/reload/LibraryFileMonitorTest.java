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

import java.io.FileOutputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.js.reload.LibraryFileMonitor;
import org.bpmscript.js.reload.LibraryToFile;

import junit.framework.TestCase;

/**
 * 
 */
public class LibraryFileMonitorTest extends TestCase {
    public void testJarLoad() throws Exception {
        URL jarResource = this.getClass().getResource("/org/springframework/integration/samples/cafe/cafeDemo.xml");
        assertEquals("jar", jarResource.getProtocol());
        String jarFileAndFile = jarResource.getFile();
        String[] split = jarFileAndFile.split("!");
        assertEquals(2, split.length);
        URL fileResource = this.getClass().getResource("/org/bpmscript/endtoend/reply.js");
        assertEquals("file", fileResource.getProtocol());
    }
    public void testCheckLibraries() throws Exception {
        LinkedBlockingQueue<ILibraryToFile> libraryAssociationQueue = new LinkedBlockingQueue<ILibraryToFile>();
        LinkedBlockingQueue<ILibraryToFile> libraryChangeQueue = new LinkedBlockingQueue<ILibraryToFile>();
        String libraryPath = "/org/bpmscript/exec/reload/library.js";
        LibraryFileMonitor libraryFileMonitor = new LibraryFileMonitor();
        libraryFileMonitor.setLibraryAssociationQueue(libraryAssociationQueue);
        libraryFileMonitor.setLibraryChangeQueue(libraryChangeQueue);
        libraryAssociationQueue.add(new LibraryToFile(libraryPath, "test"));
        libraryFileMonitor.checkLibraries();
        assertEquals(0, libraryChangeQueue.size());
        URL resource = this.getClass().getResource(libraryPath);
        FileOutputStream out = new FileOutputStream(resource.getPath());
        out.write(("Hello World! " + UUID.randomUUID().toString()).getBytes());
        out.flush();
        out.close();
        libraryFileMonitor.checkLibraries();
        libraryFileMonitor.checkLibraries();
        assertEquals(1, libraryChangeQueue.size());
    }
}
