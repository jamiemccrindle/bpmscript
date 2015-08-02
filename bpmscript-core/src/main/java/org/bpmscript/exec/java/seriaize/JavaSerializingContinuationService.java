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

package org.bpmscript.exec.java.seriaize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ICompletedResult;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.exec.java.JavaChannel;
import org.bpmscript.journal.IContinuationJournal;

/**
 * Serialises JavaChannel objects.
 */
public class JavaSerializingContinuationService {

    private IContinuationJournal continuationJournal;

    // TODO: this method of caching could result in big gc pauses followed by 
    // a complete clear of the cache...
    private Map<String, VersionAndContinuation> cachedContinuations = Collections
            .synchronizedMap(new WeakHashMap<String, VersionAndContinuation>());

    /**
     * A class that holds a version number and a continuation, used for 
     * caching a continuation against a version.
     */
    private static class VersionAndContinuation {
        private String version;
        private Object continuation;

        public VersionAndContinuation(String version, Object continuation) {
            super();
            this.version = version;
            this.continuation = continuation;
        }
    }

    /**
     * Gets the latest continuation for a particular pid and branch. If the continuation
     * is cached the cached copy is returned, otherwise it would be retrieved from the 
     * continuation journal and deserialised.
     * 
     * @param pid the instance id
     * @param branch the branch that the continuation is on
     * @return the latest continuation for particular pid and branch
     * @throws BpmScriptException if osmething goes wrong...
     */
    public JavaChannel getContinuation(String pid, String branch) throws BpmScriptException {
        String versionLatest = continuationJournal.getVersionLatest(branch);
        VersionAndContinuation versionAndContinuation = cachedContinuations.get(branch);
        if (versionAndContinuation != null && versionAndContinuation.version.equals(versionLatest)) {
            return (JavaChannel) versionAndContinuation.continuation;
        }
        byte[] continuationLatest = continuationJournal.getContinuationLatest(branch);
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(continuationLatest));
            JavaChannel javaChannel = (JavaChannel) in.readObject();
            return javaChannel;
        } catch (IOException e) {
            throw new BpmScriptException(e);
        } catch (ClassNotFoundException e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * Store the result, this involves serialising the continuation if it is a paused
     * result and then storing the result in a continuation journal.
     * 
     * @param result a class that implements a sub interface of IExecutorResult
     * @throws BpmScriptException if there is a problem storing the continuation...
     */
    public void storeResult(IExecutorResult result) throws BpmScriptException {
        if (result instanceof IPausedResult) {
            IPausedResult pausedResult = (IPausedResult) result;

            cachedContinuations.put(result.getBranch(), new VersionAndContinuation(result.getVersion(),
                    pausedResult.getContinuation()));
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            try {
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(pausedResult.getContinuation());
                out.flush();
                out.close();
                continuationJournal.storeResult(byteOut.toByteArray(), result);
            } catch (IOException e) {
                throw new BpmScriptException(e);
            }
        } else {
            if (result instanceof ICompletedResult || result instanceof IFailedResult
                    || result instanceof IKilledResult) {
                cachedContinuations.remove(result.getBranch());
            }
            continuationJournal.storeResult(null, result);
        }
    }

    /**
     * @param continuationJournal
     */
    public void setContinuationJournal(IContinuationJournal continuationJournal) {
        this.continuationJournal = continuationJournal;
    }

}
