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

package org.bpmscript.test.hibernate;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.bpmscript.process.hibernate.HibernateInstance;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.DerbyDialect;

/**
 * 
 */
public class HibernateSessionFactoryTestSupport implements ITestSupport<IServiceLookup> {

    private Class<?>[] classes;

    // dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
    // dataSource.setUrl("jdbc:derby:test;create=true");
    // dataSource.setUsername("sa");
    // dataSource.setPassword("sa");
    //    
    // Properties properties = new Properties();
    // properties.setProperty("hibernate.hbm2ddl.auto", "update");
    // properties.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
    // properties.setProperty("hibernate.query.substitutions", "true 1, false 0");
    // properties.setProperty("hibernate.show_sql", "true");

    // dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    // dataSource.setUrl("jdbc:hsqldb:mem:simulator");
    // dataSource.setUsername("sa");
    // dataSource.setPassword("");
    //    
    // Properties properties = new Properties();
    // properties.setProperty("hibernate.hbm2ddl.auto", "update");
    // properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    // properties.setProperty("hibernate.query.substitutions", "true 1, false 0");
    // properties.setProperty("hibernate.show_sql", "false");

    // final BasicDataSource dataSource = new BasicDataSource();
    // dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    // dataSource.setUrl("jdbc:mysql://localhost:3306/bpmscript");
    // dataSource.setUsername("bpmscript");
    // dataSource.setPassword("sa");
    //
    // Properties properties = new Properties();
    // properties.setProperty("hibernate.hbm2ddl.auto", "create");
    // properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    // properties.setProperty("hibernate.query.substitutions", "true 1, false 0");
    // properties.setProperty("hibernate.show_sql", "false");

    public void execute(ITestCallback<IServiceLookup> callback) throws Exception {
//        final BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
//        dataSource.setUrl("jdbc:derby:test;create=true");
//        dataSource.setUsername("sa");
//        dataSource.setPassword("sa");
//
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.hbm2ddl.auto", "update");
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
//        //properties.setProperty("hibernate.dialect", MyDerbyDialect.class.getName());
//        properties.setProperty("hibernate.show_sql", "true");
//
//        final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
//        sessionFactoryBean.setLobHandler(new DefaultLobHandler());
//        sessionFactoryBean.setHibernateProperties(properties);
//        sessionFactoryBean.setAnnotatedClasses(classes);
//        sessionFactoryBean.setDataSource(dataSource);
//        sessionFactoryBean.afterPropertiesSet();

//        SessionFactory sessionFactory = (SessionFactory) sessionFactoryBean.getObject();

//        cfg.setProperty("hibernate.dialect", DerbyDialect.class.getName());
//        cfg.setProperty("hibernate.connection.driver_class", EmbeddedDriver.class.getName());
//        cfg.setProperty("hibernate.connection.url", "jdbc:derby:lockingtest;create=true");
//        cfg.setProperty("hibernate.connection.username", "sa");
//        cfg.setProperty("hibernate.connection.password", "sa");
        
        AnnotationConfiguration cfg = new AnnotationConfiguration()
        .addAnnotatedClass(HibernateInstance.class);
        cfg.setProperty("hibernate.dialect", DerbyDialect.class.getName());
        cfg.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName());
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:~/bpmscript");
        cfg.setProperty("hibernate.connection.username", "sa");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
        cfg.setProperty("hibernate.show_sql", "true");
        for (Class<?> clazz : classes) {
            cfg.addAnnotatedClass(clazz);
        }
        final SessionFactory sessionFactory = cfg.buildSessionFactory();
        
        ServiceLookup serviceLookup = new ServiceLookup();
        serviceLookup.addService("sessionFactory", sessionFactory);
        try {
            callback.execute(serviceLookup);
        } finally {
            sessionFactory.close();
            //sessionFactoryBean.destroy();
        }
    }

    public void setClasses(Class<?>[] classes) {
        this.classes = classes;
    }
}
