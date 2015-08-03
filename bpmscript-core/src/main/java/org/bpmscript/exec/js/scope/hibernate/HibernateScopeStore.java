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

package org.bpmscript.exec.js.scope.hibernate;

import org.bpmscript.BpmScriptException;
import org.bpmscript.exec.js.scope.IScopeStore;
import org.bpmscript.js.Global;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serialises and stores scopes in a database using Hibernate
 */
public class HibernateScopeStore implements IScopeStore {

    private final SessionFactory sessionFactory;

    public HibernateScopeStore(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    public Scriptable findScope(final Context cx, final String definitionId) throws BpmScriptException {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            Criteria criteria = session.createCriteria(HibernateScope.class);
            criteria.add(Restrictions.eq("definitionId", definitionId));
            criteria.setProjection(Projections.property("scope"));
            List list = criteria.list();
            session.getTransaction().commit();
            if (list.size() == 0) {
                return null;
            }
            byte[] bytes = (byte[]) list.get(0);
            Scriptable scope = new Global(cx);
            ScriptableInputStream in = new ScriptableInputStream(new ByteArrayInputStream(bytes), scope);
            HashMap<String, Object> serialisedDiff = (HashMap<String, Object>) in.readObject();
            in.close();
            for (Map.Entry<String, Object> entry : serialisedDiff.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                ScriptableObject.putProperty(scope, key, value);
            }
            return scope;
        } catch (Exception e) {
            throw new BpmScriptException(e);
        }
    }

    public void storeScope(Context cx, String processId, Scriptable scope) throws BpmScriptException {
        try {
            ScriptableObject global = new Global(cx);
            HashMap<String, Object> diff = new HashMap<String, Object>();
            Object[] allIds = scope.getIds();
            for (Object id : allIds) {
                if (id != null) {
                    String idString = (String) id;
                    Object object = global.get(idString, global);
                    if (object == UniqueTag.NOT_FOUND) {
                        diff.put(idString, scope.get(idString, scope));
                    }
                }
            }
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ScriptableOutputStream out = new ScriptableOutputStream(byteOut, scope);
            out.writeObject(diff);
            out.flush();
            out.close();
            byte[] bytes = byteOut.toByteArray();
            Session session = sessionFactory.getCurrentSession();
            session.beginTransaction();
            session.save(new HibernateScope(processId, bytes));
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new BpmScriptException(e);
        }
    }

}
