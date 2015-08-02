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

package org.bpmscript.correlation.hibernate;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.bpmscript.benchmark.Benchmark;
import org.bpmscript.benchmark.IBenchmarkCallback;
import org.bpmscript.benchmark.IBenchmarkPrinter;
import org.bpmscript.benchmark.IWaitForCallback;
import org.bpmscript.correlation.ICorrelationChannel;
import org.bpmscript.correlation.memory.Correlation;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;

/**
 * 
 */
public class HibernateCorrelationServiceTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    /**
     * Test method for {@link org.bpmscript.correlation.hibernate.HibernateCorrelationService#addCorrelation(java.lang.String, java.lang.String, java.io.Serializable, org.bpmscript.correlation.ICorrelation)}.
     * @throws Exception 
     */
    public void testAddCorrelation() throws Exception {
        HibernateCorrelationServiceTestSupport testSupport = new HibernateCorrelationServiceTestSupport(null);
        testSupport.execute(new ITestCallback<IServiceLookup>(){
            public void execute(IServiceLookup services) throws Exception {
                HibernateCorrelationService correlationService = services.get("correlationService");
                correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken", new Correlation(), 1000000);
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.correlation.hibernate.HibernateCorrelationService#removeAllCorrelations(java.lang.String)}.
     * @throws Exception 
     */
    public void testRemoveAllCorrelations() throws Exception {
        HibernateCorrelationServiceTestSupport testSupport = new HibernateCorrelationServiceTestSupport(null);
        testSupport.execute(new ITestCallback<IServiceLookup>(){
            public void execute(IServiceLookup services) throws Exception {
                HibernateCorrelationService correlationService = services.get("correlationService");
                correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken", new Correlation(), 1000000);
                correlationService.removeAllCorrelations("groupId");
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.correlation.hibernate.HibernateCorrelationService#removeCorrelation(java.lang.String)}.
     * @throws Exception 
     */
    public void testRemoveCorrelation() throws Exception {
        HibernateCorrelationServiceTestSupport testSupport = new HibernateCorrelationServiceTestSupport(null);
        testSupport.execute(new ITestCallback<IServiceLookup>(){
            public void execute(IServiceLookup services) throws Exception {
                HibernateCorrelationService correlationService = services.get("correlationService");
                correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken", new Correlation(), 1000000);
                correlationService.removeCorrelation("correlationId");
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.correlation.hibernate.HibernateCorrelationService#send(java.lang.Object)}.
     * @throws Exception 
     */
    public void testSend() throws Exception {
        final int total = 1;
        final int criteriaTotal = 1;
        final CountDownLatch latch = new CountDownLatch(total);
        //final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        HibernateCorrelationServiceTestSupport testSupport = new HibernateCorrelationServiceTestSupport(new ICorrelationChannel() {
        
            public void send(Serializable replyToken, Object message) {
                assertEquals("replyToken", replyToken);
                log.info(latch.getCount());
                latch.countDown();
            }

            public Object getContent(Object message) {
                return null;
            }
        
        });
        testSupport.execute(new ITestCallback<IServiceLookup>(){
            public void execute(IServiceLookup services) throws Exception {
                final HibernateCorrelationService correlationService = services.get("correlationService");
                {
                    Correlation correlation = new Correlation();
                    correlation.addCriteria("message", "Hello World!");
                    correlation.addCriteria("message.length", 12);
                    correlation.addCriteria("message[0]", "H");
                    correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken", correlation, 1000000);
                }
                for(int i = 0; i < criteriaTotal; i++) {
                    Correlation correlation = new Correlation();
                    correlation.addCriteria("message", "Hello World! " + i);
                    correlation.addCriteria("message.length", i);
                    correlation.addCriteria("message[0]", "H");
                    correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken " + i, correlation, 1000000);
                }
                log.info("created all messages");
                
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                    public void execute(int count) throws Exception {
                        correlationService.send("channel", "Hello World!");
                    }
                }, new IWaitForCallback() {
                    public void call() throws Exception {
                        latch.await(360, TimeUnit.SECONDS);
                    }
                }, false));
            }
        });
    }
    
    /**
     * Test method for {@link org.bpmscript.correlation.hibernate.HibernateCorrelationService#send(java.lang.Object)}.
     * @throws Exception 
     */
    public void testXml() throws Exception {
        final int total = 1;
        final int criteriaTotal = 1;
        final CountDownLatch latch = new CountDownLatch(total);
        //final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        HibernateCorrelationServiceTestSupport testSupport = new HibernateCorrelationServiceTestSupport(new ICorrelationChannel() {
        
            public void send(Serializable replyToken, Object message) {
                assertEquals("replyToken", replyToken);
                log.info(latch.getCount());
                latch.countDown();
            }

            public Object getContent(Object message) {
                return null;
            }
        
        });
        testSupport.execute(new ITestCallback<IServiceLookup>(){
            public void execute(IServiceLookup services) throws Exception {
                final HibernateCorrelationService correlationService = services.get("correlationService");
                {
                    Correlation correlation = new Correlation();
                    correlation.addCriteria("new XML(message).text().toString()", "Hello World!");
                    correlationService.addCorrelation("channel", "groupId", "correlationId", "replyToken", correlation, 1000000);
                }
                log.info("created all messages");
                
                IBenchmarkPrinter.STDOUT.print(new Benchmark().execute(total, new IBenchmarkCallback() {
                    public void execute(int count) throws Exception {
                        correlationService.send("channel", "<xml>Hello World!</xml>");
                    }
                }, new IWaitForCallback() {
                    public void call() throws Exception {
                        latch.await(360, TimeUnit.SECONDS);
                    }
                }, false));
            }
        });
    }    
    public static void main(String[] args) throws Exception {
        HibernateCorrelationServiceTest test = new HibernateCorrelationServiceTest();;
        test.testSend();
    }

}
