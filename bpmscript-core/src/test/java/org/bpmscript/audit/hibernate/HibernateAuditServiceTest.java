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

package org.bpmscript.audit.hibernate;

import java.util.List;

import junit.framework.TestCase;

import org.bpmscript.audit.AuditMessage;
import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.Query;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.bpmscript.test.hibernate.SpringSessionFactoryTestSupport;
import org.hibernate.SessionFactory;

/**
 * Test Hibernate Audit Service
 */
public class HibernateAuditServiceTest extends TestCase {

    private static class HibernateAuditServiceTestSupport implements ITestSupport<IServiceLookup> {
        public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
            SpringSessionFactoryTestSupport springSessionFactoryTestSupport = new SpringSessionFactoryTestSupport(
                    new Class[] { HibernateAuditMessage.class });
            springSessionFactoryTestSupport.execute(new ITestCallback<IServiceLookup>() {
                public void execute(IServiceLookup services) throws Exception {
                    HibernateAuditService hibernateAuditService = new HibernateAuditService();
                    hibernateAuditService.setSessionFactory((SessionFactory) services.get("sessionFactory"));
                    hibernateAuditService.afterPropertiesSet();
                    ServiceLookup serviceLookup = new ServiceLookup(services);
                    serviceLookup.addService("auditService", hibernateAuditService);
                    callback.execute(serviceLookup);
                }
            });
        }
    }

    /**
     * Test method for
     * {@link org.bpmscript.audit.hibernate.HibernateAuditService#recordMessage(org.bpmscript.audit.IAuditMessage)}
     * .
     * 
     * @throws Exception
     */
    public void testRecordMessage() throws Exception {
        HibernateAuditServiceTestSupport testSupport = new HibernateAuditServiceTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateAuditService auditService = (HibernateAuditService) services.get("auditService");
                AuditMessage auditMessage = createMessage("messageId");
                auditService.recordMessage(auditMessage);
                IAuditMessage message = auditService.getMessage("messageId");
                assertNotNull(message);
                assertEquals("messageId", message.getMessageId());
            }
        });
    }

    /**
     * Test method for
     * {@link org.bpmscript.audit.hibernate.HibernateAuditService#getMessage(java.lang.String)}.
     * 
     * @throws Exception
     */
    public void testGetMessage() throws Exception {
        HibernateAuditServiceTestSupport testSupport = new HibernateAuditServiceTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateAuditService auditService = (HibernateAuditService) services.get("auditService");
                {
                    AuditMessage auditMessage = createMessage("messageId1");
                    auditService.recordMessage(auditMessage);
                }
                {
                    AuditMessage auditMessage = createMessage("messageId2");
                    auditService.recordMessage(auditMessage);
                }
                {
                    IAuditMessage message = auditService.getMessage("messageId1");
                    assertNotNull(message);
                    assertEquals("messageId1", message.getMessageId());
                }
                {
                    IAuditMessage message = auditService.getMessage("messageId2");
                    assertNotNull(message);
                    assertEquals("messageId2", message.getMessageId());
                }
            }

        });
    }

    /**
     * @param messageId 
     * @return
     */
    private AuditMessage createMessage(String messageId) {
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.setContent("content");
        auditMessage.setCorrelationId("correlationId");
        auditMessage.setDestination("destination");
        auditMessage.setMessageId(messageId);
        auditMessage.setReplyTo("replyTo");
        auditMessage.setTimestampLong(System.currentTimeMillis());
        return auditMessage;
    }

    /**
     * Test method for
     * {@link org.bpmscript.audit.hibernate.HibernateAuditService#findMessages(org.bpmscript.paging.IQuery)}
     * .
     * 
     * @throws Exception
     */
    public void testFindMessages() throws Exception {
        HibernateAuditServiceTestSupport testSupport = new HibernateAuditServiceTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateAuditService auditService = (HibernateAuditService) services.get("auditService");
                for(int i = 0; i < 100; i++) {
                    AuditMessage auditMessage = createMessage("messageId");
                    auditService.recordMessage(auditMessage);
                }
                IPagedResult<IAuditMessage> findMessages = auditService.findMessages(Query.ALL);
                assertNotNull(findMessages);
                assertEquals(100, findMessages.getResults().size());
            }
        });
    }

    /**
     * Test method for
     * {@link org.bpmscript.audit.hibernate.HibernateAuditService#findMessagesByDestination(org.bpmscript.paging.IQuery, java.lang.String)}
     * .
     * 
     * @throws Exception
     */
    public void testFindMessagesByDestination() throws Exception {
        HibernateAuditServiceTestSupport testSupport = new HibernateAuditServiceTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateAuditService auditService = (HibernateAuditService) services.get("auditService");
                for(int i = 0; i < 50; i++) {
                    AuditMessage auditMessage = createMessage("messageId");
                    auditMessage.setDestination("destination1");
                    auditService.recordMessage(auditMessage);
                }
                for(int i = 0; i < 50; i++) {
                    AuditMessage auditMessage = createMessage("messageId");
                    auditMessage.setDestination("destination2");
                    auditService.recordMessage(auditMessage);
                }
                IPagedResult<IAuditMessage> findMessagesByDestination = auditService.findMessagesByDestination(Query.ALL, "destination1");
                assertNotNull(findMessagesByDestination);
                assertEquals(50, findMessagesByDestination.getResults().size());
            }
        });
    }

    /**
     * Test method for
     * {@link org.bpmscript.audit.hibernate.HibernateAuditService#findDestinations()}.
     * 
     * @throws Exception
     */
    public void testFindDestinations() throws Exception {
        HibernateAuditServiceTestSupport testSupport = new HibernateAuditServiceTestSupport();
        testSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateAuditService auditService = (HibernateAuditService) services.get("auditService");
                for(int i = 0; i < 50; i++) {
                    AuditMessage auditMessage = createMessage("messageId");
                    auditMessage.setDestination("destination1");
                    auditService.recordMessage(auditMessage);
                }
                for(int i = 0; i < 50; i++) {
                    AuditMessage auditMessage = createMessage("messageId");
                    auditMessage.setDestination("destination2");
                    auditService.recordMessage(auditMessage);
                }
                List<String> findDestinations = auditService.findDestinations();
                assertNotNull(findDestinations);
                assertEquals(2, findDestinations.size());
                assertTrue(findDestinations.contains("destination1"));
                assertTrue(findDestinations.contains("destination2"));
            }
        });
    }

}
