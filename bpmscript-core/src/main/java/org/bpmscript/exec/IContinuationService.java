package org.bpmscript.exec;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * Service for serializing and deserializing continuations 
 * @author jamie
 *
 */
public interface IContinuationService {
    
    /**
     * Get a deserialized continuation back. This method will only get the latest continuation
     * on that branch back. 
     * 
     * @param invocationContext the invocation context with any variables that need to be set
     * @param scope the javascript scope to use
     * @param pid the process instance id
     * @param branch the branch of the process instance execution to use
     * @return the deserialized continuation
     * @throws BpmScriptException if something goes wrong
     */
	NativeContinuation getContinuation(Map<String, Object> invocationContext, Scriptable scope, String pid, String branch) throws BpmScriptException;
	
	/**
	 * Store a continuation
	 * 
	 * @param invocationContext any temporary variables to exclude
	 * @param excludedNames any other names to exclude, i.e. that shouldn't be serialized
	 * @param result the executor result to store
	 * @throws BpmScriptException if something goes wrong
	 */
	void storeResult(Map<String, Object> invocationContext, String[] excludedNames, IExecutorResult result) throws BpmScriptException;
}
