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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Service for reading files into Strings or Streams.
 */
// TODO: no doubt there are all sorts of character encoding issues that this
// masking and use of a singleton will make it impossible to replace this... 
public class StreamService {

	public static StreamService DEFAULT_INSTANCE = new StreamService();
	
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * Copy an {@link InputStream} into an {@link OutputStream}
     * @param in the {@link InputStream} to copy out of
     * @param out the {@link OutputStream} to copy into
     * @throws IOException if there is a problem either reading from the {@link InputStream}
     *   or writing to the {@link OutputStream}
     */
	public void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }
	
	/**
	 * Read all bytes from an {@link InputStream} and turn it into a String. Uses the 
	 * platforms character encoding.
	 * @param in the {@link InputStream} to read
	 * @return a String made up of the bytes from the {@link InputStream}
	 * @throws IOException if there was a problem reading from the stream.
	 */
	public String readFully(InputStream in) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		copyInputStream(in, result);
		return result.toString();
	}
	
	/**
	 * Get a resource from the ClassPath and turn it into a String using the platform's
	 * default Character Encoding
	 * @param name the name of the resource e.g. /org/bpmscript/blah.xml
	 * @return the contents of the resource
	 * @throws IOException if there is a problem reading the contents of the resource
	 */
	public String getResourceAsString(String name) throws IOException {
		return readFully(getClass().getResourceAsStream(name));
	}
}
