package org.bpmscript.exec.js.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ICompletedResult;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.exec.FailedResult;
import org.bpmscript.exec.IContinuationService;
import org.bpmscript.exec.PausedResult;
import org.bpmscript.journal.IContinuationJournal;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * Service for serializing continuations.
 * 
 */
public class JavascriptSerializingContinuationService implements IContinuationService {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());

    private FunctionStubService functionStubService = new FunctionStubService();
    private ObjectStubService objectStubService = new ObjectStubService();

    private IContinuationJournal continuationJournal = null;
    private Map<String, VersionAndContinuation> cachedContinuations = Collections
            .synchronizedMap(new WeakHashMap<String, VersionAndContinuation>());

    /**
     * An object kept in the cache which keeps continuations and the versions they
     * were created for.
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

    private boolean compress = true;

    /**
     * @see IContinuationService#getContinuation(Map, Scriptable, String, String)
     */
    public Continuation getContinuation(Map<String, Object> invocationContext, Scriptable scope, String pid,
            String branch) throws BpmScriptException {
        // load in the continuation

//        String versionLatest = continuationJournal.getVersionLatest(branch);
//        VersionAndContinuation versionAndContinuation = cachedContinuations.get(branch);
//        if (versionAndContinuation != null && versionAndContinuation.version.equals(versionLatest)) {
//            return (Continuation) versionAndContinuation.continuation;
//        }

        log.debug("deserializing continuation for " + pid);

        byte[] continuation = continuationJournal.getContinuationLatest(branch);
        ScriptableInputStream inputStream;
        try {
            inputStream = new StubbedInputStream(compress ? new GZIPInputStream(new ByteArrayInputStream(continuation))
                    : new ByteArrayInputStream(continuation), scope, new StubService(functionStubService
                    .getStubs(scope), objectStubService.getStubs(invocationContext)));
            // inputStream = new ScriptableInputStream(new ByteArrayInputStream(continuation),
            // scope);
            Continuation unfrozen = (Continuation) inputStream.readObject();
            return unfrozen;
        } catch (IOException e) {
            throw new BpmScriptException(e);
        } catch (ClassNotFoundException e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * @see IContinuationService#storeResult(Map, String[], IExecutorResult)
     */
    public void storeResult(Map<String, Object> invocationContext, String[] excludedNames, IExecutorResult result)
            throws BpmScriptException {

        if (result instanceof PausedResult) {
            IPausedResult pausedResult = (IPausedResult) result;
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ScriptableOutputStream outputStream;
            try {
                Scriptable topLevelScope = ScriptableObject.getTopLevelScope((Scriptable) pausedResult
                        .getContinuation());
                Scriptable continuationScope = topLevelScope.getPrototype();
                outputStream = new StubbedOutputStream(compress ? new GZIPOutputStream(byteOut) : byteOut,
                        continuationScope, new StubService(functionStubService.getStubs(topLevelScope),
                                objectStubService.getStubs(invocationContext)));

                // outputStream = new ScriptableOutputStream(byteOut, continuationScope);
                if (invocationContext != null) {
                    for (String name : invocationContext.keySet()) {
                        // outputStream.addExcludedName(name);
                        topLevelScope.delete(name);
                    }
                }

                if (excludedNames != null) {
                    for (String excludedName : excludedNames) {
                        outputStream.addExcludedName(excludedName);
                    }
                }

                outputStream.writeObject(pausedResult.getContinuation());
                outputStream.flush();
                outputStream.close();
                byte[] bytes = byteOut.toByteArray();
                continuationJournal.storeResult(bytes, result);

                cachedContinuations.put(result.getBranch(), new VersionAndContinuation(result.getVersion(),
                        pausedResult.getContinuation()));

            } catch (IOException e) {
                // TODO: this seems a little strange... i.e. we're discarding a possible result...
                continuationJournal.storeResult(null, new FailedResult(result.getPid(), result.getBranch(), result
                        .getVersion(), e, null));
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

    public IContinuationJournal getContinuationStore() {
        return continuationJournal;
    }

    public void setContinuationStore(IContinuationJournal continuationStore) {
        this.continuationJournal = continuationStore;
    }

}
