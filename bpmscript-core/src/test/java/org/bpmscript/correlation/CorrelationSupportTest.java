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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;

import org.springframework.util.StopWatch;

import junit.framework.TestCase;

/**
 * Test the correlation support class
 */
public class CorrelationSupportTest extends TestCase {

    /**
     * Test method for {@link org.bpmscript.correlation.CorrelationSupport#getHash(byte[])}.
     * @throws Exception 
     */
    public void testGetHash() throws Exception {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        HashSet<String> hashes = new HashSet<String>();
        for(int i = 0; i < 10000; i++) {
            Object[] values = new Object[3];
            values[0] = i;
            values[1] = "test " + i;
            values[2] = "randomblahblah";
            String hash = correlationSupport.getHash(correlationSupport.getBytes(values));
            if(hashes.contains(hash)) {
                fail("Duplicate hash found");
            }
            hashes.add(hash);
        }
    }
    
    public void testHashPerformance() throws Exception {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        StopWatch md5Watch = new StopWatch();
        
        Object[] values = new Object[3];
        values[0] = 100000;
        values[1] = "test ";
        values[2] = "randomblahblah";
        byte[] bytes = correlationSupport.getBytes(values);

        md5Watch.start();
        
        for(int i = 0; i < 100000; i++) {
            getHashMD5(bytes);
        }
        
        md5Watch.stop();
        
        System.out.println(md5Watch.getTotalTimeMillis());
        
        StopWatch hashCodeWatch = new StopWatch();
        
        hashCodeWatch.start();
        
        for(int i = 0; i < 100000; i++) {
            getHashHashCode(bytes);
        }
        
        hashCodeWatch.stop();
        
        System.out.println(hashCodeWatch.getTotalTimeMillis());
    }
    
    public String getHashMD5(byte[] bytes) {
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

    public String getHashHashCode(byte[] bytes) {
        return String.valueOf(Arrays.hashCode(bytes));
    }

    /**
     * Test method for {@link org.bpmscript.correlation.CorrelationSupport#getBytes(java.lang.Object)}.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void testGetBytes() throws IOException, ClassNotFoundException {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        Object[] values = new Object[3];
        values[0] = 10;
        values[1] = "test";
        values[2] = "randomblahblah";
        byte[] bytes = correlationSupport.getBytes(values);
        Object[] storedValue = (Object[]) correlationSupport.getObject(bytes);
        assertTrue(Arrays.equals(values, storedValue));
    }

    /**
     * Test method for {@link org.bpmscript.correlation.CorrelationSupport#getObject(byte[])}.
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void testGetObject() throws IOException, ClassNotFoundException {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        Object[] values = new Object[3];
        values[0] = 10;
        values[1] = "test";
        values[2] = "randomblahblah";
        byte[] bytes = correlationSupport.getBytes(values);
        Object[] storedValue = (Object[]) correlationSupport.getObject(bytes);
        assertTrue(Arrays.equals(values, storedValue));
    }

    /**
     * Test method for {@link org.bpmscript.correlation.CorrelationSupport#join(java.lang.String[])}.
     */
    public void testJoin() {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        String join = correlationSupport.join("one", "two", "three");
        String separator = correlationSupport.getSeparator();
        assertEquals("one" + separator + "two" + separator + "three", join);
    }

    /**
     * Test method for {@link org.bpmscript.correlation.CorrelationSupport#split(java.lang.String)}.
     */
    public void testSplit() {
        CorrelationSupport correlationSupport = new CorrelationSupport();
        String separator = correlationSupport.getSeparator();
        String[] split = correlationSupport.split("one" + separator + "two" + separator + "three");
        assertEquals(3, split.length);
        assertEquals("one", split[0]);
        assertEquals("two", split[1]);
        assertEquals("three", split[2]);
    }

}
