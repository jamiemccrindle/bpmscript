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

package org.bpmscript.correlation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * @see ICorrelationSupport
 */
public class CorrelationSupport implements ICorrelationSupport {

    private String separator = " +*+ ";

    /**
     * Gets the hash associated with the bytes using MD5. This is a secure
     * hashing function so could be replaced by something faster e.g.
     * the Java hashcode function
     * 
     * @see org.bpmscript.correlation.ICorrelationSupport#getHash(byte[])
     */
    public String getHash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i=0;i<digest.length;i++) {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the bytes associated with an object by serialising it
     * 
     * @see org.bpmscript.correlation.ICorrelationSupport#getBytes(java.lang.Object)
     */
    public byte[] getBytes(Object object) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out;
        out = new ObjectOutputStream(byteOut);
        out.writeObject(object);
        out.flush();
        out.close();
        return byteOut.toByteArray();
    }
    
    /* (non-Javadoc)
     * @see org.bpmscript.correlation.ICorrelationSupport#getObject(byte[])
     */
    public Object getObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object result = in.readObject();
        in.close();
        return result;
    }
    
    /**
     * Joins a number of expressions together using the separator field configured with
     * this class. Doesn't escape the expressions so there could be collisions.
     * 
     * @see org.bpmscript.correlation.ICorrelationSupport#join(java.lang.String[])
     */
    public String join(String... expressions) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            result.append(expression);
            if(i < expressions.length - 1) {
                result.append(separator);
            }
        }
        return result.toString();
    }
    
    /**
     * Splits the expressions using the separator field. Doesn't unescape the expressions
     * so there could be collisions.
     * 
     * @see org.bpmscript.correlation.ICorrelationSupport#split(java.lang.String)
     */
    public String[] split(String expressions) {
        return expressions.split(Pattern.quote(separator));
    }

    public String getSeparator() {
        return separator;
    }

    /**
     * Sets the separator that is used by the split and join methods on this class.
     * Try not to use characters that may appear in the expression language used
     * for correlation.
     * 
     * @param separator the separator characters, should not be null or empty.
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
