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

import java.io.Serializable;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.bpmscript.BpmScriptException;
import org.bpmscript.ProcessState;
import org.bpmscript.integration.Envelope;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.InvocationMessage;
import org.bpmscript.integration.internal.ResponseMessage;
import org.bpmscript.loanbroker.CreditScore;
import org.bpmscript.loanbroker.LenderGatewayRequest;
import org.bpmscript.loanbroker.LoanBrokerResponse;
import org.bpmscript.loanbroker.LoanRequest;
import org.bpmscript.loanbroker.QuoteRequest;


/**
 * ReplyProcess
 */
public class LoanBrokerProcess implements Serializable {
    
    private static final long serialVersionUID = 2030152622533266204L;

    private transient Log log;
    
    private CreditScore creditScore;
    private LoanRequest loanRequest;
    private int totalBanks = 0;
    private int bankCount = 0;
    private int bestRate = Integer.MAX_VALUE;
    private String bestBank = null;
    
    @SuppressWarnings("serial")
    public ProcessState requestBestRate(final IJavaChannel channel, Object message) throws BpmScriptException {
        loanRequest = (LoanRequest) ((Object[]) channel.getContent(message))[0];
        InvocationMessage invocationMessage = new InvocationMessage();
        invocationMessage.setArgs(channel.getParentVersion(), "loanBroker", "creditBureauGetCreditScore", new Object[] {loanRequest.getSsn()});
        Envelope envelope = new Envelope("bpmscript-first", invocationMessage);
        channel.send(envelope, 100000, new IJavaMessageHandler<IInternalMessage>() {
            public ProcessState process(IInternalMessage message) throws BpmScriptException {
                // TODO: why is the content a string? ah because it has timed out!
                creditScore = (CreditScore) channel.getContent(message);
                InvocationMessage invocationMessage = new InvocationMessage();
                invocationMessage.setArgs(channel.getParentVersion(), "loanBroker", "lenderGateway", new Object[] {new LenderGatewayRequest(loanRequest.getAmount(), creditScore)});
                Envelope envelope = new Envelope("bpmscript-first", invocationMessage);
                channel.send(envelope, 100000, new IJavaMessageHandler<IInternalMessage>() {
                    public ProcessState process(IInternalMessage message) throws BpmScriptException {
                        String[] banks = (String[]) channel.getContent(message);
                        totalBanks = banks.length;
                        for (final String bank : banks) {
                            InvocationMessage invocationMessage = new InvocationMessage();
                            invocationMessage.setArgs(channel.getParentVersion(), "loanBroker", "bankGetLoanQuote", new Object[] {new QuoteRequest(bank, loanRequest.getAmount(), creditScore, loanRequest.getSsn(), loanRequest.getTerm())});
                            Envelope envelope = new Envelope("bpmscript-first", invocationMessage);
                            channel.send(envelope, 100000, new IJavaMessageHandler<IInternalMessage>() {
                                public ProcessState process(IInternalMessage message) throws BpmScriptException {
                                    bankCount++;
                                    int rate = (Integer) channel.getContent(message);
                                    if(rate < bestRate) {
                                        bestBank = bank;
                                        bestRate = rate;
                                    }
                                    if(bankCount == totalBanks) {
                                        ResponseMessage responseMessage = new ResponseMessage();
                                        responseMessage.setContent(new LoanBrokerResponse(bestBank, bestRate));
                                        channel.reply(responseMessage);
                                        return COMPLETED;
                                    } else {
                                        return PAUSED;
                                    }
                                }
                                public ProcessState timeout() throws BpmScriptException {
                                    throw new BpmScriptException("Timed out");
                                }
                            });
                        }
                        return PAUSED;
                    }
                    public ProcessState timeout() throws BpmScriptException {
                        throw new BpmScriptException("Timed out");
                    }
                });
                return PAUSED;
            }
            public ProcessState timeout() throws BpmScriptException {
                throw new BpmScriptException("Timed out");
            }
        });
        return PAUSED;
    }
    public ProcessState creditBureauGetCreditScore(final IJavaChannel channel, Object message) throws BpmScriptException {
        ResponseMessage response = new ResponseMessage();
        response.setContent(new CreditScore(10, 10));
        channel.reply(response);
        return COMPLETED;
    }
    public ProcessState lenderGateway(final IJavaChannel channel, Object message) throws BpmScriptException {
        ResponseMessage response = new ResponseMessage();
        response.setContent(new String[] {"bank1", "bank2", "bank3"});
        channel.reply(response);
        return COMPLETED;
    }
    public ProcessState bankGetLoanQuote(final IJavaChannel channel, Object message) throws BpmScriptException {
        ResponseMessage response = new ResponseMessage();
        response.setContent(new Random(System.currentTimeMillis()).nextInt(10));
        channel.reply(response);
        return COMPLETED;
    }
    public CreditScore getCreditScore() {
        return creditScore;
    }
    public LoanRequest getLoanRequest() {
        return loanRequest;
    }
    public int getTotalBanks() {
        return totalBanks;
    }
    public int getBankCount() {
        return bankCount;
    }
    public int getBestRate() {
        return bestRate;
    }
    public String getBestBank() {
        return bestBank;
    }
    public Log getLog() {
        return log;
    }
    public void setLog(Log log) {
        this.log = log;
    }
}
