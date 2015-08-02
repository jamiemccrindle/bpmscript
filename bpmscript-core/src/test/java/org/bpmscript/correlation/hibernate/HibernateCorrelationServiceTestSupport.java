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

import org.bpmscript.correlation.ICorrelationChannel;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;
import org.bpmscript.test.hibernate.SpringSessionFactoryTestSupport;
import org.hibernate.SessionFactory;

/**
 * Test support for the HibernateCorrelationService.
 */
public class HibernateCorrelationServiceTestSupport implements ITestSupport<IServiceLookup> {

    private ICorrelationChannel correlationChannel = null;
    
    public HibernateCorrelationServiceTestSupport(ICorrelationChannel correlationChannel) {
        this.correlationChannel = correlationChannel;
    }

    public void execute(final ITestCallback<IServiceLookup> callback) throws Exception {
        SpringSessionFactoryTestSupport springSessionFactoryTestSupport = new SpringSessionFactoryTestSupport(new Class[] {HibernateCorrelation.class});
        springSessionFactoryTestSupport.execute(new ITestCallback<IServiceLookup>() {
            public void execute(IServiceLookup services) throws Exception {
                HibernateCorrelationService hibernateCorrelationService = new HibernateCorrelationService();
                hibernateCorrelationService.setChannel(correlationChannel);
                hibernateCorrelationService.setSessionFactory((SessionFactory) services.get("sessionFactory"));
                hibernateCorrelationService.afterPropertiesSet();
                ServiceLookup serviceLookup = new ServiceLookup(services);
                serviceLookup.addService("correlationService", hibernateCorrelationService);
                callback.execute(serviceLookup);
            }
        });
    }

}
