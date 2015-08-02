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

/**
 * Strategy interface for correlation services for useful methods like hashing,
 * splitting and joining expressions.
 */
public interface ICorrelationSupport {

    /**
     * Get the hash for these bytes. The hash does not have to be secure.
     * 
     * @param bytes the bytes used to generate the hash. should not be null.
     * @return the hash for the bytes.
     */
    String getHash(byte[] bytes);

    /**
     * Gets the bytes associated with an object. Must be the mirror of getObject
     * @see #getObject(byte[])
     * 
     * @param object
     * @return
     * @throws IOException
     */
    byte[] getBytes(Object object) throws IOException;

    /**
     * Gets an object for a set of bytes. Must 
     * @see #getBytes(Object)

     * @param bytes the bytes to turn back into an object
     * @return the object that results
     * @throws IOException if there was a problem deserializing the bytes
     * @throws ClassNotFoundException if the class associated with the 
     *   bytes could not be found..
     */
    Object getObject(byte[] bytes) throws IOException, ClassNotFoundException;

    /**
     * Joins a number of expressions together. Mirror of "split". Expressions
     * could be something like:
     * 
     * message.getInvoiceId()
     * message.getQuote()
     * 
     * and the joining of them could be:
     * 
     * message.getInvoiceId() || message.getQuote
     * 
     * ideally provision should be made for escaping the joining characters
     * within the expressions so that there aren't collisions.
     * 
     * @see #split(String)
     * @param expressions the expressions to join
     * @return the expressions joined into a string.
     */
    String join(String... expressions);

    /**
     * Splits an expression string into one or more expressions. A mirror 
     * of the join method
     * 
     * @see #join(String...)
     * @param expressions the expressions to split
     * @return the expressions split into an array
     */
    String[] split(String expressions);

}