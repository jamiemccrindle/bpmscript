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
 * LoanRequest dto for the LoanBroker
 */
public class LoanRequest implements Serializable {
    /**
     * String ssn, int term, int amount
     */
    private static final long serialVersionUID = -2885540141270790577L;
    private String ssn;
    private int term;
    private int amount;

    public LoanRequest() {
    }

    public LoanRequest(String ssn, int term, int amount) {
        super();
        this.ssn = ssn;
        this.term = term;
        this.amount = amount;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
