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

package org.bpmscript.timeout;

import java.io.Serializable;

/**
 * Utility class for creating millisecond level timeout
 */
public class TimeCalculator implements Serializable {
    
    public static final TimeCalculator DEFAULT_INSTANCE = new TimeCalculator();
    
    private static final long serialVersionUID = -1446235559387583260L;
    
    /**
     * @param minutes how many minutes to return milliseconds for
     * @return minutes * seconds(60)
     */
    public long minutes(long minutes) {
        return minutes * seconds(60); 
    }
    
    /**
     * @param seconds how many seconds to return milliseconds for
     * @return seconds * 1000
     */
    public long seconds(long seconds) {
        return seconds * 1000;
    }
    
    /**
     * @param hours how many hours to return milliseconds for
     * @return hours * minutes(60)
     */
    public long hours(long hours) {
        return hours * minutes(60);
    }
    
    /**
     * @param days how many days to return millseconds for
     * @return days * hours(24)
     */
    public long days(long days) {
        return days * hours(24);
    }
}
