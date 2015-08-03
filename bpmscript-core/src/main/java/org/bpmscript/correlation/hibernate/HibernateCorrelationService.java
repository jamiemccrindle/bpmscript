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

import org.bpmscript.correlation.*;
import org.bpmscript.js.DynamicContextFactory;
import org.bpmscript.js.Global;
import org.bpmscript.js.IJavascriptSourceCache;
import org.bpmscript.util.StreamService;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.mozilla.javascript.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Uses Hibernate as a backing store for correlations. The current design is to add the correlation to the store
 * and then delete it when the correlation is removed. It might be a good idea to consider timeouts at some point
 * even if they're relatively long...
 * <p/>
 * The question is the race condition between selecting all of the relevant correlations and deleting them i.e.
 * we'd get a locking problem. We'll have to work out the right isolation level for the select so that it doesn't
 * compete with the delete. One other way to do it would be with write only but then we have an issue with data
 * growing indefinitely (we could lock for the delete and do it on a much reduced timescale... bit like garbage
 * collection...).
 */
public class HibernateCorrelationService implements ICorrelationService {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private final SessionFactory sessionFactory;
    private final ICorrelationChannel channel;
    private final ICorrelationSupport correlationSupport = new CorrelationSupport();
    private final IJavascriptSourceCache javascriptSourceCache;
    private final Resource library;

    public HibernateCorrelationService(
            SessionFactory sessionFactory,
            ICorrelationChannel channel,
            IJavascriptSourceCache javascriptSourceCache,
            Resource library) {
        this.sessionFactory = sessionFactory;
        this.channel = channel;
        this.javascriptSourceCache = javascriptSourceCache;
        this.library = library;
    }

    public void addCorrelation(String channelName, String groupId, String correlationId, Serializable replyToken, ICorrelation key, long timeout) throws CorrelationException {
        try {
            byte[] replyTokenBytes = correlationSupport.getBytes(replyToken);
            List<ICorrelationCriteria> criteria = key.getCriteria();
            String[] expressions = new String[criteria.size()];
            Object[] values = new Object[criteria.size()];
            int i = 0;
            for (ICorrelationCriteria criterion : criteria) {
                expressions[i] = criterion.getExpression();
                values[i] = criterion.getValue();
                i++;
            }
            byte[] valuesBytes = correlationSupport.getBytes(values);
            String valuesHash = correlationSupport.getHash(valuesBytes);
            HibernateCorrelation hibernateCorrelation = new HibernateCorrelation();
            hibernateCorrelation.setChannel(channelName);
            hibernateCorrelation.setCorrelationId(correlationId);
            hibernateCorrelation.setGroupId(groupId);
            hibernateCorrelation.setExpressions(correlationSupport.join(expressions));
            hibernateCorrelation.setReplyToken(replyTokenBytes);
            hibernateCorrelation.setValueObjects(valuesBytes);
            hibernateCorrelation.setValuesHash(valuesHash);
            if (timeout > 0) {
                hibernateCorrelation.setTimeout(System.currentTimeMillis() + timeout);
            } else {
                hibernateCorrelation.setTimeout(Long.MAX_VALUE);
            }
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            session.save(hibernateCorrelation);
            session.getTransaction().commit();
        } catch (IOException e) {
            throw new CorrelationException(e);
        }
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#removeAllCorrelations(java.lang.String)
     */
    public void removeAllCorrelations(final String groupId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("delete from " + HibernateCorrelation.class.getName() + " where groupId = :groupId");
        query.setString("groupId", groupId);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#removeCorrelation(java.lang.String)
     */
    public void removeCorrelation(final String correlationId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Query query = session.createQuery("delete from " + HibernateCorrelation.class.getName() + " where correlationId = :correlationId");
        query.setString("correlationId", correlationId);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    public List<Object> send(final String channelName, final Object message) throws CorrelationException {
        List<Object> matches = new ArrayList<Object>();
        Context cx = new DynamicContextFactory().enterContext();
        Global scope = new Global(cx);
        ScriptableObject.putProperty(scope, "message", Context.javaToJS(message, scope));
        ScriptableObject.putProperty(scope, "content", Context.javaToJS(channel.getContent(message), scope));
        try {
            if (library != null && javascriptSourceCache != null) {
                Script configScript = javascriptSourceCache.getScript(
                        StreamService.DEFAULT_INSTANCE.readFully(library.getInputStream()), library.getDescription());
                configScript.exec(cx, scope);
            } else {
                log.debug("either library or javascriptSourceCache are not set");
            }
            //scope.sealObject();
            List<String> result = getDistinctExpressions(channelName);
            HashMap<String, Script> scriptCache = new HashMap<String, Script>();
            for (String expressions : result) {
                String[] expressionsArray = correlationSupport.split(expressions);
                Object[] values = new Object[expressionsArray.length];
                try {
                    int i = 0;
                    for (String expression : expressionsArray) {
                        Script script = scriptCache.get(expression);
                        if (script == null) {
                            script = cx.compileReader(new StringReader(expression), expression, 0, null);
                            scriptCache.put(expression, script);
                        }
                        Object scriptResult = script.exec(cx, scope);
                        if (scriptResult instanceof NativeJavaObject) {
                            scriptResult = ((NativeJavaObject) scriptResult).unwrap();
                        }
                        values[i++] = scriptResult;
                    }
                    // we found the values. now we have to find out if we have any matching criteria
                    byte[] valuesBytes = correlationSupport.getBytes(values);
                    String valuesHash = correlationSupport.getHash(valuesBytes);
                    List<HibernateCorrelation> results = getHibernateCorrelationByValueHash(channelName, valuesHash);
                    // if there are any matching results, go through them and check that the values actually match... would be odd if they didn't
                    // but you can't be sure...
                    for (HibernateCorrelation storedCorrelation : results) {
                        byte[] storedValuesBytes = storedCorrelation.getValueObjects();
                        Object[] storedValues = (Object[]) correlationSupport.getObject(storedValuesBytes);
                        if (Arrays.equals(values, storedValues)) {
                            // we have a winner
                            log.debug(expressions + " matched with value " + values + " for " + storedCorrelation.getCorrelationId());
                            matches.add((Serializable) correlationSupport.getObject(storedCorrelation.getReplyToken()));
                        }
                    }
                } catch (NullPointerException e) {
                    log.debug(e, e);
                } catch (IOException e) {
                    log.error(e, e);
                } catch (EcmaError e) {
                    log.error(e, e);
                } catch (Throwable t) {
                    log.error(t, t);
                }
            }
            return matches;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Context.exit();
        }

    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<String> getDistinctExpressions(String channelName) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(HibernateCorrelation.class);
        criteria.setProjection(Projections.distinct(Projections.property("expressions")));
        criteria.add(Restrictions.eq("channel", channelName));
        criteria.add(Restrictions.gt("timeout", System.currentTimeMillis()));
        List<String> result = criteria.list();
        session.getTransaction().commit();
        return result;
    }

    /**
     * @param valuesHash
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<HibernateCorrelation> getHibernateCorrelationByValueHash(String channel, String valuesHash) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<HibernateCorrelation> results = session.createCriteria(HibernateCorrelation.class)
                .add(Restrictions.eq("valuesHash", valuesHash))
                .add(Restrictions.eq("channel", channel))
                .list();
        session.getTransaction().commit();
        return results;
    }

}
