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

package org.bpmscript.exec.js.serialize;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

/**
 * 
 */
public class MonitoringOutputStream extends OutputStream {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private OutputStream out;
    private MultiMap bytesRun = new MultiHashMap();
    private int counter = 0;

    public MonitoringOutputStream(OutputStream out) {
        super();
        this.out = out;
    }

    public void close() throws IOException {
        log.debug("close()");
        out.close();
    }

    public void flush() throws IOException {
        log.debug("flush()");
        out.flush();
    }

    @SuppressWarnings("unchecked")
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        log.debug("write(byte[] arg0, int arg1, int arg2) " + new String(arg0, arg1, arg2));
        byte[] bytes = new byte[arg2 - arg1];
        System.arraycopy(arg0, arg1, bytes, 0, arg2 - arg1);
        bytesRun.put(new BytesRun(bytes), counter++);
        out.write(arg0, arg1, arg2);
    }

    public void write(byte[] arg0) throws IOException {
        log.debug("write(byte[] arg0) " + new String(arg0));
        out.write(arg0);
    }

    public void write(int arg0) throws IOException {
        log.debug("write(int arg0) " + (char) arg0);
        out.write(arg0);
    }

    public MultiMap getBytesRun() {
        return bytesRun;
    }
}
