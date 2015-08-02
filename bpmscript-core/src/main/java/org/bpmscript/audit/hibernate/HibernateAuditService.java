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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bpmscript.audit.AuditMessage;
import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.bpmscript.audit.IAuditMessageService;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.process.hibernate.HibernatePagingService;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 */
public class HibernateAuditService extends HibernateDaoSupport implements IAuditMessageRecorder, IAuditMessageService {

    private HibernatePagingService pagingService = new HibernatePagingService();
    
    /**
     * @see org.bpmscript.audit.IAuditMessageRecorder#recordMessage(org.bpmscript.audit.IAuditMessage)
     */
    public void recordMessage(IAuditMessage auditMessage) {
        HibernateAuditMessage message = new HibernateAuditMessage();
        message.setContent(auditMessage.getContent());
        message.setCorrelationId(auditMessage.getCorrelationId());
        message.setDestination(auditMessage.getDestination());
        message.setReplyTo(auditMessage.getReplyTo());
        message.setMessageId(auditMessage.getMessageId());
        message.setTimestampLong(auditMessage.getTimestampLong());
        getHibernateTemplate().save(message);
    }

    /**
     * @see org.bpmscript.audit.hibernate.IAuditMessageService#getMessage(java.lang.String)
     */
    public IAuditMessage getMessage(final String messageId) {
        return (IAuditMessage) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateAuditMessage.class)
                    .add(Expression.eq("messageId", messageId));
                List list = criteria.list();
                if(list.size() == 0) {
                    return null;
                } else {
                    return list.get(0);
                }
            }
        });
    }
    
    /**
     * @see org.bpmscript.audit.hibernate.IAuditMessageService#findMessages(org.bpmscript.paging.IQuery)
     */
    @SuppressWarnings("unchecked")
    public IPagedResult<IAuditMessage> findMessages(final IQuery query) {
        return (IPagedResult<IAuditMessage>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = pagingService.createCriteria(session, HibernateAuditMessage.class, query);
                criteria.setProjection(Projections.projectionList()
                        .add(Projections.property("messageId"))
                        .add(Projections.property("correlationId"))
                        .add(Projections.property("destination"))
                        .add(Projections.property("replyTo"))
                        .add(Projections.property("timestampLong"))
                        );
                List list = criteria.list();
                List<IAuditMessage> result = new ArrayList<IAuditMessage>();
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    Object[] row = (Object[]) iterator.next();
                    AuditMessage auditMessage = new AuditMessage(null, (String) row[1], (String) row[2], (String) row[0], (String) row[3], (Long) row[4]);
                    result.add(auditMessage);
                }
                return new PagedResult<IAuditMessage>(result, query.getMaxResults());
            }
        });
    }

    /**
     * @see org.bpmscript.audit.hibernate.IAuditMessageService#findMessages(org.bpmscript.paging.IQuery)
     */
    @SuppressWarnings("unchecked")
    public IPagedResult<IAuditMessage> findMessagesByDestination(final IQuery query, final String destination) {
        return (IPagedResult<IAuditMessage>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = pagingService.createCriteria(session, HibernateAuditMessage.class, query);
                criteria.setProjection(Projections.projectionList()
                        .add(Projections.property("messageId"))
                        .add(Projections.property("correlationId"))
                        .add(Projections.property("destination"))
                        .add(Projections.property("replyTo"))
                        .add(Projections.property("timestampLong"))
                        );
                criteria.add(Expression.eq("destination", destination));
                List list = criteria.list();
                List<IAuditMessage> result = new ArrayList<IAuditMessage>();
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    Object[] row = (Object[]) iterator.next();
                    AuditMessage auditMessage = new AuditMessage(null, (String) row[1], (String) row[2], (String) row[0], (String) row[3], (Long) row[4]);
                    result.add(auditMessage);
                }
                return new PagedResult<IAuditMessage>(result, query.getMaxResults());
            }
        });
    }

    /**
     * @see org.bpmscript.audit.hibernate.IAuditMessageService#findMessages(org.bpmscript.paging.IQuery)
     */
    @SuppressWarnings("unchecked")
    public List<String> findDestinations() {
        return (List<String>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateAuditMessage.class);
                criteria.setProjection(Projections.distinct(Projections.projectionList()
                        .add(Projections.property("destination"))
                        ));
                List list = criteria.list();
                return list;
            }
        });
    }

    public void setPagingService(HibernatePagingService pagingService) {
        this.pagingService = pagingService;
    }

}
