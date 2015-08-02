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

/**
 * Immutable implementation of the {@link ILibraryToFile} value object interface.
 * 
 * Associates a library and file
 */
public class LibraryToFile implements ILibraryToFile {

    private String library;

    private String file;

    /**
     * Create a new value object with a library and file
     * @param library the library 
     * @param file the file
     */
    public LibraryToFile(String library, String file) {
        super();
        this.library = library;
        this.file = file;
    }

    public String getLibrary() {
        return library;
    }

    public String getFile() {
        return file;
    }
}
