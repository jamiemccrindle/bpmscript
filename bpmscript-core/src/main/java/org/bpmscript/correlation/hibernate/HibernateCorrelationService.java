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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bpmscript.correlation.CorrelationException;
import org.bpmscript.correlation.CorrelationSupport;
import org.bpmscript.correlation.ICorrelation;
import org.bpmscript.correlation.ICorrelationChannel;
import org.bpmscript.correlation.ICorrelationCriteria;
import org.bpmscript.correlation.ICorrelationService;
import org.bpmscript.correlation.ICorrelationSupport;
import org.bpmscript.js.DynamicContextFactory;
import org.bpmscript.js.Global;
import org.bpmscript.js.IJavascriptSourceCache;
import org.bpmscript.util.StreamService;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Uses Hibernate as a backing store for correlations. The current design is to add the correlation to the store
 * and then delete it when the correlation is removed. It might be a good idea to consider timeouts at some point
 * even if they're relatively long...
 * 
 * The question is the race condition between selecting all of the relevant correlations and deleting them i.e. 
 * we'd get a locking problem. We'll have to work out the right isolation level for the select so that it doesn't
 * compete with the delete. One other way to do it would be with write only but then we have an issue with data
 * growing indefinitely (we could lock for the delete and do it on a much reduced timescale... bit like garbage 
 * collection...).
 */
public class HibernateCorrelationService extends HibernateDaoSupport implements ICorrelationService {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private ICorrelationChannel channel = null;
    private ICorrelationSupport correlationSupport = new CorrelationSupport();
    private IJavascriptSourceCache javascriptSourceCache;
    private Resource library;
    
    public void setLibrary(Resource libraryResource) {
        this.library = libraryResource;
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#addCorrelation(java.lang.String, java.lang.String, java.io.Serializable, org.bpmscript.correlation.ICorrelation)
     */
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
            if(timeout > 0) {
                hibernateCorrelation.setTimeout(System.currentTimeMillis() + timeout);
            } else {
                hibernateCorrelation.setTimeout(Long.MAX_VALUE);
            }
            getHibernateTemplate().save(hibernateCorrelation);
        } catch (IOException e) {
            throw new CorrelationException(e);
        }
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#removeAllCorrelations(java.lang.String)
     */
    public void removeAllCorrelations(final String groupId) {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("delete from " + HibernateCorrelation.class.getName() + " where groupId = :groupId");
                query.setString("groupId", groupId);
                query.executeUpdate();
                return null;
            }
        });
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#removeCorrelation(java.lang.String)
     */
    public void removeCorrelation(final String correlationId) {
        getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("delete from " + HibernateCorrelation.class.getName() + " where correlationId = :correlationId");
                query.setString("correlationId", correlationId);
                query.executeUpdate();
                return null;
            }
        });
    }

    /**
     * @see org.bpmscript.correlation.ICorrelationService#send(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public List<Object> send(final String channelName, final Object message) throws CorrelationException {
        return (List<Object>) getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                List<Object> matches = new ArrayList<Object>();
                Context cx = new DynamicContextFactory().enterContext();
                Global scope = new Global(cx);
                ScriptableObject.putProperty(scope, "message", Context.javaToJS(message, scope));
                ScriptableObject.putProperty(scope, "content", Context.javaToJS(channel.getContent(message), scope));
                try {
                    if(library != null && javascriptSourceCache != null) {
                        Script configScript = javascriptSourceCache.getScript(
                                StreamService.DEFAULT_INSTANCE.readFully(library.getInputStream()), library.getDescription());
                        configScript.exec(cx, scope);
                    } else {
                        log.debug("either library or javascriptSourceCache are not set");
                    }
                    //scope.sealObject();
                    List<String> result = getDistinctExpressions(channelName, session);
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
                                if(scriptResult instanceof NativeJavaObject) {
                                    scriptResult = ((NativeJavaObject) scriptResult).unwrap();
                                }
                                values[i++] = scriptResult;
                            }
                            // we found the values. now we have to find out if we have any matching criteria
                            byte[] valuesBytes = correlationSupport.getBytes(values);
                            String valuesHash = correlationSupport.getHash(valuesBytes);
                            List<HibernateCorrelation> results = getHibernateCorrelationByValueHash(session, channelName, valuesHash);
                            // if there are any matching results, go through them and check that the values actually match... would be odd if they didn't
                            // but you can't be sure...
                            for (HibernateCorrelation storedCorrelation : results) {
                                byte[] storedValuesBytes = storedCorrelation.getValueObjects();
                                Object[] storedValues = (Object[]) correlationSupport.getObject(storedValuesBytes);
                                if(Arrays.equals(values, storedValues)) {
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

        });
    }

    /**
     * @param session
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<String> getDistinctExpressions(String channelName, Session session) {
        Criteria criteria = session.createCriteria(HibernateCorrelation.class);
        criteria.setProjection(Projections.distinct(Projections.property("expressions")));
        criteria.add(Expression.eq("channel", channelName));
        criteria.add(Expression.gt("timeout", System.currentTimeMillis()));
        List<String> result = criteria.list();
        return result;
    }

    public ICorrelationChannel getChannel() {
        return channel;
    }

    public void setChannel(ICorrelationChannel channel) {
        this.channel = channel;
    }

    public void setCorrelationSupport(ICorrelationSupport correlationSupport) {
        this.correlationSupport = correlationSupport;
    }

    /**
     * @param session
     * @param valuesHash
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<HibernateCorrelation> getHibernateCorrelationByValueHash(Session session, String channel, String valuesHash) {
        List<HibernateCorrelation> results = session.createCriteria(HibernateCorrelation.class)
            .add(Expression.eq("valuesHash", valuesHash))
            .add(Expression.eq("channel", channel))
            .list();
        return results;
    }

}
