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

package org.bpmscript.process.hibernate;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import org.bpmscript.IExecutorResult;
import org.bpmscript.exec.IgnoredResult;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.hibernate.HibernateSessionFactoryTestSupport;
import org.hibernate.SessionFactory;

/**
 * Hibernate instance manager test
 */
public class HibernateInstanceManagerTest extends TestCase {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    /**
     * Test creating and locking an instance
     * @throws Exception
     */
    public void testInstanceManagerCreateAndLock() throws Exception {
        HibernateSessionFactoryTestSupport support = new HibernateSessionFactoryTestSupport();
        support.setClasses(new Class[] { HibernateInstance.class });
        support.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                SessionFactory sessionFactory = services.get("sessionFactory");
                final HibernateInstanceManager instanceManager = new HibernateInstanceManager();
                instanceManager.setSessionFactory(sessionFactory);
                final String pid1 = instanceManager.createInstance("parentVersion", "definitionId", "definitionName", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "one");
                IInstance instance = instanceManager.getInstance(pid1);
                assertNotNull(instance);

                final String pid2 = instanceManager.createInstance("parentVersion", "definitionId", "definitionName", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "two");
                assertNotNull(pid2);
                final AtomicReference<Queue<String>> results = new AtomicReference<Queue<String>>(
                        new LinkedList<String>());
                instanceManager.doWithInstance(pid1, new IInstanceCallback() {
                    public IExecutorResult execute(IInstance instance) throws Exception {
                        assertNotNull(instance);
                        log.info("locking one");
                        results.get().add("one");
                        return new IgnoredResult("", "", "");
                    }
                });
            }

        });
    }

    /**
     * test creating and locking an instance using two different threads
     * @throws Exception
     */
    public void testInstanceManager() throws Exception {
        HibernateSessionFactoryTestSupport support = new HibernateSessionFactoryTestSupport();
        support.setClasses(new Class[] { HibernateInstance.class });
        support.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                SessionFactory sessionFactory = services.get("sessionFactory");
                final HibernateInstanceManager instanceManager = new HibernateInstanceManager();
                instanceManager.setSessionFactory(sessionFactory);
                final String pid1 = instanceManager.createInstance("parentVersion", "definitionId", "definitionName", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "one");
                IInstance instance = instanceManager.getInstance(pid1);
                assertNotNull(instance);

                final String pid2 = instanceManager.createInstance("parentVersion", "definitionId", "definitionName", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "two");
                assertNotNull(pid2);
                ExecutorService executorService = Executors.newCachedThreadPool();
                final AtomicReference<Queue<String>> results = new AtomicReference<Queue<String>>(
                        new LinkedList<String>());
                executorService.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        return instanceManager.doWithInstance(pid1, new IInstanceCallback() {
                            public IExecutorResult execute(IInstance instance) throws Exception {
                                log.warn("locking one");
                                Thread.sleep(2000);
                                log.warn("finished sleeping one");
                                results.get().add("one");
                                return new IgnoredResult("", "", "");
                            }
                        });
                    }

                });
                Thread.sleep(100);
                Future<Object> future2 = executorService.submit(new Callable<Object>() {

                    public Object call() throws Exception {
                        return instanceManager.doWithInstance(pid1, new IInstanceCallback() {
                            public IExecutorResult execute(IInstance instance) throws Exception {
                                log.warn("locking two");
                                results.get().add("two");
                                return new IgnoredResult("", "", "");
                            }
                        });
                    }

                });
                future2.get();
                assertEquals(2, results.get().size());
                assertEquals("one", results.get().poll());
                assertEquals("two", results.get().poll());
            }

        });
    }
}
