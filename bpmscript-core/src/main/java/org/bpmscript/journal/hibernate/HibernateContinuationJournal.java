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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.journal.IContinuationJournalEntry;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores continuation entries in a database using Hibernate as the ORM
 */
@Transactional(readOnly = true)
public class HibernateContinuationJournal extends HibernateDaoSupport implements IContinuationJournal {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    /**
     * Search for an entry with this version, create a new branch and copy the the whole continuation
     * entry onto the new branch. 
     * 
     * @see org.bpmscript.journal.IContinuationJournal#createBranch(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String createBranch(final String version) {
        List<HibernateContinuationJournalEntry> branchVersions = getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "bv");
                criteria.add(Expression.eq("version", version));
                criteria.setMaxResults(1);
                return criteria.list();
            }
        });
        HibernateContinuationJournalEntry existingBranchVersion = branchVersions.get(0);

        String branch = UUID.randomUUID().toString();
        
        HibernateContinuationJournalEntry hibernateContinuationJournalEntry = new HibernateContinuationJournalEntry();
        hibernateContinuationJournalEntry.setContinuation(existingBranchVersion.getContinuation());
        hibernateContinuationJournalEntry.setInstanceId(existingBranchVersion.getInstanceId());
        hibernateContinuationJournalEntry.setState(existingBranchVersion.getState());
        hibernateContinuationJournalEntry.setVersion(version);
        hibernateContinuationJournalEntry.setBranch(branch);
        getHibernateTemplate().save(hibernateContinuationJournalEntry);
        
        return branch;
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#createMainBranch(java.lang.String)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String createMainBranch(String pid) {
        String branch = UUID.randomUUID().toString();
        log.debug("created branch " + branch + " for pid " + pid);
        return branch;
    }

    /**
     * Creates a new unique UUID as a new version number. The version is only stored when
     * storeResult is called.
     * 
     * @see org.bpmscript.journal.IContinuationJournal#createVersion(java.lang.String, java.lang.String)
     */
    public String createVersion(String pid, String branch) {
        return UUID.randomUUID().toString();
    }

    /**
     * Selects all branches for associated with a pid.
     * 
     * @see org.bpmscript.journal.IContinuationJournal#getBranchesForPid(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getBranchesForPid(final String pid) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class);
                criteria.setProjection(Projections.distinct(Projections.property("branch")));
                criteria.add(Expression.eq("instanceId", pid));
                return criteria.list();
            }
        });
    }

    /**
     * Get the latest version on a branch. Orders by the natural id of this entity.
     * 
     * @param branch the branch to look up, must not be null.
     * @return the latest version for the branch.
     */
    public String getLatestVersion(final String branch) {
        return (String) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class);
                criteria.setProjection(Projections.property("version"));
                criteria.add(Expression.eq("branch", branch));
                criteria.addOrder(Order.desc("id"));
                criteria.setMaxResults(1);
                return criteria.list().get(0);
            }
        });
    }

    /**
     * Selects the continuation associated with the latest entry on a branch.
     * 
     * @see org.bpmscript.journal.IContinuationJournal#getContinuationLatest(java.lang.String)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public byte[] getContinuationLatest(final String branch) {
        return (byte[]) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "ex");
                criteria.setProjection(Projections.property("ex.continuation"));
                criteria.add(Expression.eq("ex.branch", branch));
                criteria.addOrder(Order.desc("ex.id"));
                criteria.setMaxResults(1);
                return criteria.list().get(0);
            }
        });
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getLiveResults(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<IContinuationJournalEntry> getLiveResults(final String pid) {
        return (Collection<IContinuationJournalEntry>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "ex");
                criteria.add(Expression.eq("ex.instanceId", pid));
                criteria.add(Expression.in("ex.state", new Object[] {ProcessState.CREATED.name(), ProcessState.RUNNING.name(), ProcessState.PAUSED.name()}));
                return criteria.list();
            }
        });
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getProcessStateLatest(java.lang.String)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public ProcessState getProcessStateLatest(final String branch) {
        log.debug("getting process state for " + branch);
        return ProcessState.valueOf((String) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "ex");
                criteria.setProjection(Projections.property("ex.state"));
                criteria.add(Expression.eq("ex.branch", branch));
                criteria.addOrder(Order.desc("ex.id"));
                criteria.setMaxResults(1);
                List list = criteria.list();
                if(list.size() < 1) {
                    log.warn("nothing returned for branch " + branch);
                }
                return list.get(0);
            }
        }));
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#isDirty(java.lang.String)
     */
    public String getVersionLatest(final String branch) {
        log.debug("getting latests version for branch " + branch);
        return (String) getHibernateTemplate().execute(new HibernateCallback() {
            @SuppressWarnings("unchecked")
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "ex");
                criteria.setProjection(Projections.property("ex.version"));
                criteria.add(Expression.eq("ex.branch", branch));
                criteria.addOrder(Order.desc("ex.id"));
                criteria.setMaxResults(1);
                List list = criteria.list();
                if(list.size() < 1) {
                    log.warn("nothing returned for branch " + branch);
                }
                return list.get(0);
            }
        });
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#storeResult(byte[], org.bpmscript.IExecutorResult)
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void storeResult(final byte[] continuation, final IExecutorResult result) {
        log.debug("storing result for pid " + result.getPid() + " branch " + result.getBranch() + " version " + result.getVersion());
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                HibernateContinuationJournalEntry branchVersion = new HibernateContinuationJournalEntry();
                branchVersion.setBranch(result.getBranch());
                branchVersion.setContinuation(continuation);
                branchVersion.setState(result.getProcessState().name());
                branchVersion.setInstanceId(result.getPid());
                branchVersion.setVersion(result.getVersion());
                session.save(branchVersion);
                return null;
            }
        });
    }

    /**
     * @see org.bpmscript.journal.IContinuationJournal#getResults(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<IContinuationJournalEntry> getEntriesForPid(final String pid) {
        return (Collection<IContinuationJournalEntry>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(HibernateContinuationJournalEntry.class, "ex");
                criteria.add(Expression.eq("ex.instanceId", pid));
                return criteria.list();
            }
        });
    }

}
