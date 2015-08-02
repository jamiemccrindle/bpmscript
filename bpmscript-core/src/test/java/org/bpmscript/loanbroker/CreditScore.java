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

package org.bpmscript.loanbroker;

import java.io.Serializable;

/**
 * Credit score dto for the loanbroaker
 */
public class CreditScore implements Serializable {
    private int score;
    private int hlength;

    public CreditScore() {
    }

    public CreditScore(int score, int hlength) {
        super();
        this.score = score;
        this.hlength = hlength;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getHlength() {
        return hlength;
    }

    public void setHlength(int hlength) {
        this.hlength = hlength;
    }
}
