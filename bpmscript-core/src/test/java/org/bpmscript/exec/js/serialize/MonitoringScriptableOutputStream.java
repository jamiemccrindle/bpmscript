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
import java.io.ObjectStreamClass;
import java.io.OutputStream;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * 
 */
public class MonitoringScriptableOutputStream extends ScriptableOutputStream {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    @Override
    public void flush() throws IOException {
        log.info("flush");
        super.flush();
    }

    @Override
    public void reset() throws IOException {
        log.info("reset");
        super.reset();
    }

    @Override
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        log.info("write(byte[] arg0, int arg1, int arg2)");
        super.write(arg0, arg1, arg2);
    }

    @Override
    public void write(byte[] arg0) throws IOException {
        log.info("write(byte[] arg0)");
        super.write(arg0);
    }

    @Override
    public void write(int arg0) throws IOException {
        log.info("write(int arg0)");
        super.write(arg0);
    }

    @Override
    public void writeBoolean(boolean arg0) throws IOException {
        log.info("writeBoolean(boolean arg0)");
        super.writeBoolean(arg0);
    }

    @Override
    public void writeByte(int arg0) throws IOException {
        log.info("writeByte(int arg0)");
        super.writeByte(arg0);
    }

    @Override
    public void writeBytes(String arg0) throws IOException {
        log.info("writeBytes(String arg0)");
        super.writeBytes(arg0);
    }

    @Override
    public void writeChar(int arg0) throws IOException {
        log.info("writeChar(int arg0)");
        super.writeChar(arg0);
    }

    @Override
    public void writeChars(String arg0) throws IOException {
        log.info("writeChars(String arg0)");
        super.writeChars(arg0);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass arg0) throws IOException {
        log.info("writeClassDescriptor(ObjectStreamClass arg0)");
        super.writeClassDescriptor(arg0);
    }

    @Override
    public void writeDouble(double arg0) throws IOException {
        log.info("writeDouble(double arg0)");
        super.writeDouble(arg0);
    }

    @Override
    public void writeFields() throws IOException {
        log.info("writeFields()");
        super.writeFields();
    }

    @Override
    public void writeFloat(float arg0) throws IOException {
        log.info("writeFloat(float arg0)");
        super.writeFloat(arg0);
    }

    @Override
    public void writeInt(int arg0) throws IOException {
        log.info("writeInt(int arg0)");
        super.writeInt(arg0);
    }

    @Override
    public void writeLong(long arg0) throws IOException {
        log.info("writeLong(long arg0)");
        super.writeLong(arg0);
    }

    @Override
    protected void writeObjectOverride(Object arg0) throws IOException {
        log.info("writeObjectOverride(Object arg0)");
        super.writeObjectOverride(arg0);
    }

    @Override
    public void writeShort(int arg0) throws IOException {
        log.info("writeShort(int arg0)");
        super.writeShort(arg0);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // log.info("writeStreamHeader()");
        super.writeStreamHeader();
    }

    @Override
    public void writeUnshared(Object arg0) throws IOException {
        log.info("writeUnshared(Object arg0)");
        super.writeUnshared(arg0);
    }

    @Override
    public void writeUTF(String arg0) throws IOException {
        log.info("writeUTF(String arg0)");
        super.writeUTF(arg0);
    }

    public MonitoringScriptableOutputStream(OutputStream out, Scriptable scope) throws IOException {
        super(out, scope);
    }

}
