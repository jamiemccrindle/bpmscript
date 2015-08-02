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

package org.bpmscript.exec.java;

import static org.bpmscript.ProcessState.COMPLETED;
import static org.bpmscript.ProcessState.PAUSED;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.loanbroker.CreditScore;
import org.bpmscript.loanbroker.LoanBrokerResponse;
import org.bpmscript.loanbroker.LoanRequest;
import org.bpmscript.loanbroker.QuoteRequest;

/**
 */
public class MethodLoanBrokerProcess {
    
    interface ILoanBrokerServices {
        /**
         * @Channel(address="bpmscript-first", responseMethod="onCreditBureauGetCreditScore", timeout=1212312)
         * @param ssn
         */
        void creditBureauGetCreditScore(String ssn);
        void lenderGatewayGetBanks(int amount, CreditScore creditScore);
        void bankGetRate(QuoteRequest quoteRequest);
    }

    private InternalBpmScriptChannel channel;
    private int bestRate = Integer.MAX_VALUE;
    private int bankCount = 0;
    private int totalBanks = 0;
    private String bestBank;

    public void requestBestRate(LoanRequest loanRequest) throws BpmScriptException {
        channel.send(10000, new MethodInvocationHandler(this, "onCreditBureauCreditScore", loanRequest),
                "creditBureauGetCreditScore", loanRequest.getSsn());
    }

    public void onCreditBureauCreditScore(CreditScore creditScore, LoanRequest loanRequest)
            throws BpmScriptException {
        channel.send(10000, new MethodInvocationHandler(this, "onLenderGatewayGetBanks", loanRequest, creditScore),
                "lenderGatewayGetBanks", loanRequest.getAmount(), creditScore);
    }

    public void onLenderGatewayGetBanks(String[] banks, LoanRequest loanRequest, CreditScore creditScore)
            throws BpmScriptException {
        totalBanks = banks.length;
        for (final String bank : banks) {
            channel.send(10000, new MethodInvocationHandler(this, "onBank"), "bankGetRate", new QuoteRequest(
                    bank, loanRequest.getAmount(), creditScore, loanRequest.getSsn(), loanRequest.getTerm()));
        }
    }

    public ProcessState onBank(int rate, String bank) {
        bankCount++;
        if (rate < bestRate) {
            bestBank = bank;
            bestRate = rate;
        }
        if (bankCount == totalBanks) {
            channel.reply(new LoanBrokerResponse(bestBank, bestRate));
            return COMPLETED;
        } else {
            return PAUSED;
        }
    }
}
