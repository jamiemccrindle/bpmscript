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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceManager;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate instance manager
 */
public class HibernateInstanceManager extends HibernateDaoSupport implements IInstanceManager {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private HibernatePagingService pagingService = HibernatePagingService.DEFAULT_INSTANCE;

    /**
     * @see org.bpmscript.process.IInstanceManager#createInstance(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String createInstance(String parentVersion, String definitionId, String definitionName, String definitionType, String operation) throws BpmScriptException {
        HibernateInstance instance = new HibernateInstance();
        instance.setCreationDate(new Timestamp(System.currentTimeMillis()));
        instance.setDefinitionId(definitionId);
        instance.setOperation(operation);
        instance.setDefinitionName(definitionName);
        instance.setDefinitionType(definitionType);
        instance.setParentVersion(parentVersion);
        getHibernateTemplate().save(instance);
        return instance.getId();
    }

    /**
     * @see org.bpmscript.process.IInstanceManager#doWithInstance(java.lang.String,
     *      org.bpmscript.process.IInstanceCallback)
     */
    public IExecutorResult doWithInstance(final String pid, final IInstanceCallback callback) throws Exception {
        return (IExecutorResult) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Transaction transaction = session.beginTransaction();
                try {
                    HibernateInstance instance = (HibernateInstance) session.load(HibernateInstance.class, pid, LockMode.FORCE);
                    log.debug("locking " + pid);
                    Object result = callback.execute(instance);
                    log.debug("unlocking " + pid);
                    transaction.commit();
                    return result;
                } catch (Exception t) {
                    transaction.rollback();
                    throw new HibernateException(t);
                } catch (Throwable t) {
                    transaction.rollback();
                    throw new RuntimeException(t);
                }
            }

        });
    }

    /**
     * @see org.bpmscript.process.IInstanceManager#getChildInstances(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<IInstance> getChildInstances(final String parentVersion) throws BpmScriptException {
        return getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateInstance.class);
                criteria.add(Expression.eq("parentVersion", parentVersion));
                List<IInstance> list = criteria.list();
                return list;
            }
        });
    }

    /**
     * @see org.bpmscript.process.IInstanceManager#getInstance(java.lang.String)
     */
    public IInstance getInstance(String pid) throws BpmScriptException {
        return (IInstance) getHibernateTemplate().get(HibernateInstance.class, pid);
    }

    /**
     * @see org.bpmscript.process.IInstanceManager#getInstances(org.bpmscript.paging.IQuery)
     */
    @SuppressWarnings("unchecked")
    public IPagedResult<IInstance> getInstances(final IQuery query) throws BpmScriptException {
        return (IPagedResult<IInstance>) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = pagingService.createCriteria(session, HibernateInstance.class, query);
                return new PagedResult<IInstance>(criteria.list(), query.getMaxResults());
            }

        });
    }

    /**
     * @see org.bpmscript.process.IInstanceManager#getInstancesForDefinition(org.bpmscript.paging.IQuery,
     *      java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public IPagedResult<IInstance> getInstancesForDefinition(final IQuery query, final String definitionId)
            throws BpmScriptException {
        return (IPagedResult<IInstance>) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(org.hibernate.Session session) throws HibernateException, SQLException {
                Criteria criteria = pagingService.createCriteria(session, HibernateInstance.class, query);
                criteria.add(Expression.eq("definitionId", definitionId));
                return new PagedResult<IInstance>(criteria.list(), query.getMaxResults());
            }

        });
    }

    public void setPagingService(HibernatePagingService pagingService) {
        this.pagingService = pagingService;
    }

}
