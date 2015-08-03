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

import org.bpmscript.audit.AuditMessage;
import org.bpmscript.audit.IAuditMessage;
import org.bpmscript.audit.IAuditMessageRecorder;
import org.bpmscript.audit.IAuditMessageService;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.process.hibernate.HibernatePagingService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */
public class HibernateAuditService implements IAuditMessageRecorder, IAuditMessageService {

    private final SessionFactory sessionFactory;
    private HibernatePagingService pagingService = new HibernatePagingService();

    public HibernateAuditService(
            SessionFactory sessionFactory,
            HibernatePagingService pagingService) {
        this.sessionFactory = sessionFactory;
        this.pagingService = pagingService;
    }

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

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.save(message);
        session.getTransaction().commit();
    }

    public IAuditMessage getMessage(final String messageId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(HibernateAuditMessage.class)
                .add(Restrictions.eq("messageId", messageId));
        List list = criteria.list();
        session.getTransaction().commit();
        if(list.size() == 0) {
            return null;
        } else {
            IAuditMessage auditMessage = (IAuditMessage) list.get(0);
            return auditMessage;
        }
    }
    
    public IPagedResult<IAuditMessage> findMessages(final IQuery query) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria criteria = pagingService.createCriteria(session, HibernateAuditMessage.class, query);
        criteria.setProjection(Projections.projectionList()
                        .add(Projections.property("messageId"))
                        .add(Projections.property("correlationId"))
                        .add(Projections.property("destination"))
                        .add(Projections.property("replyTo"))
                        .add(Projections.property("timestampLong"))
        );
        List list = criteria.list();
        session.getTransaction().commit();
        List<IAuditMessage> result = new ArrayList<IAuditMessage>();
        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            Object[] row = (Object[]) iterator.next();
            AuditMessage auditMessage = new AuditMessage(null, (String) row[1], (String) row[2], (String) row[0], (String) row[3], (Long) row[4]);
            result.add(auditMessage);
        }
        return new PagedResult<IAuditMessage>(result, query.getMaxResults());
    }

    public IPagedResult<IAuditMessage> findMessagesByDestination(final IQuery query, final String destination) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria criteria = pagingService.createCriteria(session, HibernateAuditMessage.class, query);
        criteria.setProjection(Projections.projectionList()
                        .add(Projections.property("messageId"))
                        .add(Projections.property("correlationId"))
                        .add(Projections.property("destination"))
                        .add(Projections.property("replyTo"))
                        .add(Projections.property("timestampLong"))
        );
        criteria.add(Restrictions.eq("destination", destination));
        List list = criteria.list();
        session.getTransaction().commit();
        List<IAuditMessage> result = new ArrayList<IAuditMessage>();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object[] row = (Object[]) iterator.next();
            AuditMessage auditMessage = new AuditMessage(null, (String) row[1], (String) row[2], (String) row[0], (String) row[3], (Long) row[4]);
            result.add(auditMessage);
        }
        return new PagedResult<IAuditMessage>(result, query.getMaxResults());
    }

    public List<String> findDestinations() {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(HibernateAuditMessage.class);
        criteria.setProjection(Projections.distinct(Projections.projectionList()
                        .add(Projections.property("destination"))
        ));
        List list = criteria.list();
        session.getTransaction().commit();
        return list;
    }

}
