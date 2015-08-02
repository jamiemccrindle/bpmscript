package org.bpmscript.exec.js;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bpmscript.IExecutorResult;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Calls a callback and creates the appropriate {@link IExecutorResult} result depending
 * on how the callback returns e.g. if it throws an exception, an IFailedResult.
 */
public interface IJavascriptExecutor {
	
    /**
     * 
     * @param pid
     * @param branch
     * @param version
     * @param invocationContext
     * @param excludedNames
     * @param cx
     * @param scope
     * @param callback
     * @return
     * @throws IOException
     */
	IExecutorResult execute(String definitionName, String pid, String branch, String version, Map<String, Object> invocationContext, List<String> excludedNames, Context cx,
			Scriptable scope, Callable<Object> callback) throws IOException;

}
