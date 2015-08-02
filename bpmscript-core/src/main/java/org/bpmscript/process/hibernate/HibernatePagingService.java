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

import java.util.List;

import org.bpmscript.paging.IOrderBy;
import org.bpmscript.paging.IQuery;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

/**
 * Helper class for paging
 */
public class HibernatePagingService {
    public static final HibernatePagingService DEFAULT_INSTANCE = new HibernatePagingService();
    
    /**
     * Creates a criteria class based on a paged query
     * @param session the Hibernate session
     * @param clazz the class that is being searched
     * @param query the paged query
     * @return a Hibernate criteria object.
     */
    public Criteria createCriteria(Session session, Class<?> clazz, IQuery query) {
        Criteria criteria = session.createCriteria(clazz);
        if(query.getFirstResult() > 0) {
            criteria.setFirstResult(query.getFirstResult());
        }
        if(query.getMaxResults() > 0) {
            criteria.setMaxResults(query.getMaxResults() + 1);
        }
        List<IOrderBy> orderBys = query.getOrderBys();
        if(orderBys != null) {
            for (IOrderBy orderBy : orderBys) {
                criteria.addOrder(orderBy.isAsc() ? Order.asc(orderBy.getField()) : Order.desc(orderBy.getField()));
            }
        }
        return criteria;
    }
}
