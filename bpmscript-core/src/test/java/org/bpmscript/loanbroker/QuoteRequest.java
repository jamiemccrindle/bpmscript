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
 * 
 */
public class QuoteRequest implements Serializable {
    
    private String bank;
    private int amount;
    private CreditScore creditScore;
    private String ssn;
    private int term;
    
    public QuoteRequest(String bank, int amount, CreditScore creditScore, String ssn, int term) {
        super();
        this.bank = bank;
        this.amount = amount;
        this.creditScore = creditScore;
        this.ssn = ssn;
        this.term = term;
    }
    public String getBank() {
        return bank;
    }
    public void setBank(String bank) {
        this.bank = bank;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public CreditScore getCreditScore() {
        return creditScore;
    }
    public void setCreditScore(CreditScore creditScore) {
        this.creditScore = creditScore;
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
    
}
