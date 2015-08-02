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

package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;
import org.bpmscript.hibernate.DerbyDialect;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * Locking test
 */
public class HibernateDerbyLockingTest extends TestCase {

    public static interface IConnectionCallback {
        public void execute(Connection connection) throws Exception;
    }

    public static interface IStatementCallback {
        public void execute(Statement statement) throws Exception;
    }

    public static class DerbyTemplate {

        public void doWithStatement(final IStatementCallback callback)
                throws Exception {
            doWithConnection(new IConnectionCallback() {
                public void execute(Connection connection) throws Exception {
                    Statement statement = connection.createStatement();
                    try {
                        callback.execute(statement);
                    } finally {
                        statement.close();
                    }
                }
            });
        }

        public void doWithConnection(IConnectionCallback callback)
                throws Exception {
            Class.forName(EmbeddedDriver.class.getName());
            Connection connection = DriverManager.getConnection(
                    "jdbc:derby:lockingtest;create=true", "sa", "sa");
            connection.setAutoCommit(false);
            // connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            try {
                callback.execute(connection);
                connection.commit();
            } finally {
                connection.close();
            }
        }

    }

    public void testJDBC() throws Exception {
        final DerbyTemplate template = new DerbyTemplate();
        try {
            template.doWithStatement(new IStatementCallback() {
                public void execute(Statement statement) throws Exception {
                    statement.execute("CREATE TABLE TEST(\n"
                            + "ID CHAR(36) NOT NULL,\n" + "NAME VARCHAR(255)\n"
                            + ")");
                }
            });
            template.doWithStatement(new IStatementCallback() {
                public void execute(Statement statement) throws Exception {
                    statement
                            .execute("INSERT INTO TEST(ID, NAME) VALUES('12345', 'Bob')");
                }
            });

            final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

            ExecutorService executorService = Executors.newCachedThreadPool();
            Future<?> submit = executorService.submit(new Callable<Object>() {

                public Object call() throws Exception {
                    template.doWithStatement(new IStatementCallback() {
                        public void execute(Statement statement)
                                throws Exception {
                            ResultSet resultSet = statement
                                    .executeQuery("SELECT ID, NAME FROM TEST WHERE ID = '12345' for update with rs");
                            while (resultSet.next()) {
                                String id = resultSet.getString("ID");
                                String name = resultSet.getString("NAME");
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (Throwable t) {
                            }
                            System.out.println("one");
                            queue.add("one");
                            try {
                                Thread.sleep(500);
                            } catch (Throwable t) {
                            }
                        }
                    });
                    return null;
                }
            });
            Thread.sleep(500);
            Future<?> submit2 = executorService.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    template.doWithStatement(new IStatementCallback() {
                        public void execute(Statement statement)
                                throws Exception {
                            ResultSet resultSet = statement
                                    .executeQuery("SELECT ID, NAME FROM TEST WHERE ID = '12345' for update with rr");
                            while (resultSet.next()) {
                                String id = resultSet.getString("ID");
                                String name = resultSet.getString("NAME");
                            }
                            queue.add("two");
                            System.out.println("two");
                        }
                    });
                    return null;
                }
            });
            submit.get();
            submit2.get();
            assertEquals("one", queue.poll(3, TimeUnit.SECONDS));
            assertEquals("two", queue.poll(3, TimeUnit.SECONDS));
        } finally {
            template.doWithStatement(new IStatementCallback() {
                public void execute(Statement statement) throws Exception {
                    statement.execute("DROP TABLE TEST");
                }
            });
        }
    }

    public void testSpring() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create");
        properties.setProperty("hibernate.dialect",
                "org.bpmscript.hibernate.DerbyDialect");
        properties.setProperty("hibernate.show_sql", "true");

        properties.setProperty("hibernate.connection.driver_class",
                EmbeddedDriver.class.getName());
        properties.setProperty("hibernate.connection.url",
                "jdbc:derby:lockingtest;create=true");
        properties.setProperty("hibernate.connection.username", "sa");
        properties.setProperty("hibernate.connection.password", "sa");
        properties.setProperty("hibernate.connection.pool_size", "10");

        final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setLobHandler(new DefaultLobHandler());
        sessionFactoryBean.setHibernateProperties(properties);
        sessionFactoryBean.setAnnotatedClasses(new Class[] { Person.class });
        sessionFactoryBean.afterPropertiesSet();

        SessionFactory sessionFactory = (SessionFactory) sessionFactoryBean
                .getObject();
        try {
            runTest(sessionFactory);
        } finally {
            sessionFactory.close();
        }
    }

    public void testSpringWithDataSource() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(EmbeddedDriver.class.getName());
        dataSource.setUrl("jdbc:derby:lockingtest;create=true");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create");
        properties.setProperty("hibernate.dialect",
                "org.bpmscript.hibernate.DerbyDialect");
        properties.setProperty("hibernate.show_sql", "true");

        // properties.setProperty("hibernate.connection.driver_class",
        // EmbeddedDriver.class.getName());
        // properties.setProperty("hibernate.connection.url",
        // "jdbc:derby:lockingtest;create=true");
        // properties.setProperty("hibernate.connection.username", "sa");
        // properties.setProperty("hibernate.connection.password", "sa");
        // properties.setProperty("hibernate.connection.pool_size", "10");

        final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setLobHandler(new DefaultLobHandler());
        sessionFactoryBean.setHibernateProperties(properties);
        sessionFactoryBean.setAnnotatedClasses(new Class[] { Person.class });
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.afterPropertiesSet();

        SessionFactory sessionFactory = (SessionFactory) sessionFactoryBean
                .getObject();
        try {
            runTest(sessionFactory);
        } finally {
            sessionFactory.close();
        }
    }

    public void runTest(final SessionFactory sessionFactory) throws Exception {
        Person person = new Person();

        Session session = sessionFactory.openSession();
        session.save(person);
        session.flush();
        session.close();

        final String id = person.getId();
        final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<?> submit = executorService.submit(new Runnable() {
            public void run() {
                Session session = sessionFactory.openSession();
                Transaction transaction = session.beginTransaction();
                session.load(Person.class, id, LockMode.UPGRADE);
                try {
                    Thread.sleep(2000);
                } catch (Throwable t) {
                }
                System.out.println("one");
                queue.add("one");
                try {
                    Thread.sleep(500);
                } catch (Throwable t) {
                }
                transaction.commit();
                session.flush();
                session.close();
            }
        });
        Thread.sleep(500);
        Future<?> submit2 = executorService.submit(new Runnable() {
            public void run() {
                Session session = sessionFactory.openSession();
                Transaction transaction = session.beginTransaction();
                session.load(Person.class, id, LockMode.UPGRADE);
                queue.add("two");
                System.out.println("two");
                transaction.commit();
                session.flush();
                session.close();
            }
        });
        submit.get();
        submit2.get();
        assertEquals("one", queue.poll(3, TimeUnit.SECONDS));
        assertEquals("two", queue.poll(3, TimeUnit.SECONDS));
    }

    public void testHibernate() throws Exception {
        AnnotationConfiguration cfg = new AnnotationConfiguration()
                .addAnnotatedClass(Person.class);
        cfg.setProperty("hibernate.dialect", DerbyDialect.class.getName());
        cfg.setProperty("hibernate.connection.driver_class",
                EmbeddedDriver.class.getName());
        cfg.setProperty("hibernate.connection.url",
                "jdbc:derby:lockingtest;create=true");
        cfg.setProperty("hibernate.connection.username", "sa");
        cfg.setProperty("hibernate.connection.password", "sa");
        cfg.setProperty("hibernate.connection.pool_size", "10");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
        cfg.setProperty("hibernate.show_sql", "true");
        final SessionFactory sessionFactory = cfg.buildSessionFactory();

        try {
            runTest(sessionFactory);
        } finally {
            sessionFactory.close();
        }
    }
}
