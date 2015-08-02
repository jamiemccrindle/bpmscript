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

package org.bpmscript.journal.hibernate;

import java.util.Collection;

import junit.framework.TestCase;

import org.bpmscript.ProcessState;
import org.bpmscript.exec.PausedResult;
import org.bpmscript.journal.hibernate.HibernateContinuationJournal;
import org.bpmscript.journal.hibernate.HibernateContinuationJournalEntry;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.bpmscript.test.hibernate.HibernateSessionFactoryTestSupport;
import org.hibernate.SessionFactory;

/**
 * 
 */
public class HibernateContinuationJournalTest extends TestCase {
    
    public class HibernateContinuationJournalTestSupport implements ITestSupport<IServiceLookup> {
        private HibernateSessionFactoryTestSupport hibernateSessionFactoryTestSupport = new HibernateSessionFactoryTestSupport();
        public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
            hibernateSessionFactoryTestSupport.setClasses(new Class[] {
                HibernateContinuationJournalEntry.class
            });
            hibernateSessionFactoryTestSupport.execute(new ITestCallback<IServiceLookup>() {
                public void execute(IServiceLookup services) throws Exception {
                    HibernateContinuationJournal continationJournal = new HibernateContinuationJournal();
                    continationJournal.setSessionFactory((SessionFactory) services.get("sessionFactory"));
                    continationJournal.afterPropertiesSet();
                    ServiceLookup serviceLookup = new ServiceLookup(services);
                    serviceLookup.addService("continuationJournal", continationJournal);
                    callback.execute(serviceLookup);
                }
            });
        }
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#createBranch(java.lang.String)}.
     * @throws Exception 
     */
    public void testCreateBranch() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version, null));
                String newBranch = continuationJournal.createBranch(version);
                assertNotNull(newBranch);
                Collection<String> branchesForPid = continuationJournal.getBranchesForPid("1234");
                assertEquals(2, branchesForPid.size());
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#createMainBranch(java.lang.String)}.
     * @throws Exception 
     */
    public void testCreateMainBranch() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, "1111", null));
                Collection<String> branchesForPid = continuationJournal.getBranchesForPid("1234");
                assertEquals(1, branchesForPid.size());
                assertEquals(mainBranch, branchesForPid.iterator().next());
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#createVersion(java.lang.String, java.lang.String)}.
     * @throws Exception 
     */
    public void testCreateVersion() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version = continuationJournal.createVersion("1234", mainBranch);
                assertNotNull(version);
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#getBranchesForPid(java.lang.String)}.
     * @throws Exception 
     */
    public void testGetBranchesForPid() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version, null));
                String newBranch1 = continuationJournal.createBranch(version);
                String newBranch2 = continuationJournal.createBranch(version);
                Collection<String> branchesForPid = continuationJournal.getBranchesForPid("1234");
                assertEquals(3, branchesForPid.size());
                assertTrue(branchesForPid.contains(mainBranch));
                assertTrue(branchesForPid.contains(newBranch1));
                assertTrue(branchesForPid.contains(newBranch2));
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#getLatestVersion(java.lang.String)}.
     * @throws Exception 
     */
    public void testGetLatestVersion() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version1 = continuationJournal.createVersion("1234", mainBranch);
                assertNotNull(version1);
                String version2 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version1, null));
                String version3 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version2, null));
                String version4 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version3, null));
                String newBranch2 = continuationJournal.createBranch(version3);
                String version5 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version4, null));
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version5, null));
                String latestVersion = continuationJournal.getLatestVersion(mainBranch);
                assertEquals(version5, latestVersion);
                String latestBranchVersion = continuationJournal.getLatestVersion(newBranch2);
                assertEquals(version3, latestBranchVersion);
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#getContinuationLatest(java.lang.String)}.
     * @throws Exception 
     */
    public void testGetContinuationLatest() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version1 = continuationJournal.createVersion("1234", mainBranch);
                assertNotNull(version1);
                String version2 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version2, null));
                byte[] continuationLatest = continuationJournal.getContinuationLatest(mainBranch);
                assertEquals("test", new String(continuationLatest));
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#getProcessStateLatest(java.lang.String)}.
     * @throws Exception 
     */
    public void testGetProcessStateLatest() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version1 = continuationJournal.createVersion("1234", mainBranch);
                assertNotNull(version1);
                String version2 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version2, null));
                ProcessState processStateLatest = continuationJournal.getProcessStateLatest(mainBranch);
                assertEquals(processStateLatest, ProcessState.PAUSED);
            }
        });
    }

    /**
     * Test method for {@link org.bpmscript.journal.hibernate.HibernateContinuationJournal#storeResult(byte[], org.bpmscript.IExecutorResult)}.
     * @throws Exception 
     */
    public void testStoreResult() throws Exception {
        HibernateContinuationJournalTestSupport hibernateContinuationJournalTestSupport = new HibernateContinuationJournalTestSupport();
        hibernateContinuationJournalTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup serviceLookup) throws Exception {
                HibernateContinuationJournal continuationJournal = serviceLookup.get("continuationJournal");
                String mainBranch = continuationJournal.createMainBranch("1234");
                String version1 = continuationJournal.createVersion("1234", mainBranch);
                assertNotNull(version1);
                String version2 = continuationJournal.createVersion("1234", mainBranch);
                continuationJournal.storeResult("test".getBytes(), new PausedResult("1234", mainBranch, version2, null));
            }
        });
    }

}
