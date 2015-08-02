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

import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.H2Dialect;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

/**
 * 
 */
public class SpringSessionFactoryTestSupport implements ITestSupport<IServiceLookup> {

    private final Class<?>[] classes;

    public SpringSessionFactoryTestSupport(Class<?>... classes) {
        super();
        this.classes = classes;
    }

    // <bean id="databaseDriver" class="java.lang.String"><constructor-arg
    // value="org.h2.Driver"/></bean>
    // <bean id="databaseUrl" class="java.lang.String"><constructor-arg
    // value="jdbc:h2:~/bpmscript"/></bean>
    // <bean id="databaseUsername" class="java.lang.String"><constructor-arg value="sa"/></bean>
    // <bean id="databasePassword" class="java.lang.String"><constructor-arg value=""/></bean>
    // <bean id="databaseDialect" class="java.lang.String"><constructor-arg
    // value="org.hibernate.dialect.H2Dialect"/></bean>

    public void execute(ITestCallback<IServiceLookup> callback) throws Exception {

//         final BasicDataSource dataSource = new BasicDataSource();
//         dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//         dataSource.setUrl("jdbc:mysql://localhost:3306/bpmscript");
//         dataSource.setUsername("bpmscript");
//         dataSource.setPassword("sa");
//        
//         Properties properties = new Properties();
//         properties.setProperty("hibernate.hbm2ddl.auto", "create");
//         properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
//         properties.setProperty("hibernate.show_sql", "false");

         final BasicDataSource dataSource = new BasicDataSource();
         dataSource.setDriverClassName("org.h2.Driver");
         dataSource.setUrl("jdbc:h2:~/bpmscript");
         dataSource.setUsername("sa");
         dataSource.setPassword("");
        
         Properties properties = new Properties();
         properties.setProperty("hibernate.hbm2ddl.auto", "create");
         properties.setProperty("hibernate.dialect", H2Dialect.class.getName());
         properties.setProperty("hibernate.show_sql", "false");
        
//        final BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
//        dataSource.setUrl("jdbc:derby:test;create=true");
//        dataSource.setUsername("sa");
//        dataSource.setPassword("sa");
//
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.hbm2ddl.auto", "update");
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
//        properties.setProperty("hibernate.query.substitutions", "true 1, false 0");
//        properties.setProperty("hibernate.show_sql", "false");

        ServiceLookup lookup = new ServiceLookup();
        final AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setLobHandler(new DefaultLobHandler());
        sessionFactoryBean.setHibernateProperties(properties);
        sessionFactoryBean.setAnnotatedClasses(classes);
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.afterPropertiesSet();

        SessionFactory sessionFactory = (SessionFactory) sessionFactoryBean.getObject();
        lookup.addService("sessionFactory", sessionFactory);

        try {
            callback.execute(lookup);
        } finally {
            sessionFactory.close();
            sessionFactoryBean.destroy();
        }
    }

}
