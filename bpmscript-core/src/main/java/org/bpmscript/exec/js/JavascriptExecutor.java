package org.bpmscript.exec.js;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bpmscript.IExecutorResult;
import org.bpmscript.exec.CompletedResult;
import org.bpmscript.exec.ContinuationError;
import org.bpmscript.exec.FailedResult;
import org.bpmscript.exec.KillError;
import org.bpmscript.exec.KilledResult;
import org.bpmscript.exec.PausedResult;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.NativeContinuation;

/**
 * A Javascript Process Executor
 */
public class JavascriptExecutor implements IJavascriptExecutor {

	private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(getClass());
	
	private StackTraceParser stackTraceParser = new StackTraceParser();
	
	/**
	 * Executes the callback and transforms the result into the appropriate IExecutorResult subclass (PAUSED, FAILED, KILLED, COMPLETED)
	 */
	public IExecutorResult execute(String definitionName, String pid, String branch, String version, Map<String, Object> invocationContext, List<String> excludedNames, Context cx,
			Scriptable scope, Callable<Object> callback) throws IOException {

		try {
			Object result = callback.call();
			return new CompletedResult(pid, branch, version, result);
        } catch (ContinuationError error) {
			NativeContinuation cont = error.getContinuation();
//            return new PausedResult(pid, branch, version, cont, stackTraceParser
//                    .getScriptStackTrace(error.getEvaluatorException()));
            return new PausedResult(pid, branch, version, cont);
        } catch (KillError error) {
            return new KilledResult(pid, branch, version, error.getMessage());
		} catch (JavaScriptException ex) {
			// otherwise, if it's a regular exception throw it
			Exception exception = new Exception(ex.getMessage());
			exception.setStackTrace(ex.getStackTrace());
            return new FailedResult(pid, branch, version, exception, stackTraceParser
                    .getScriptStackTrace(ex));
		} catch (WrappedException e) {
			return new FailedResult(pid, branch, version, e.getCause(), stackTraceParser
					.getScriptStackTrace(e));
		} catch (EcmaError e) {
			log.warn(definitionName + ":" + e, e);
			return new FailedResult(pid, branch, version, new SerializeableEcmaError(e), null);
		} catch (Throwable e) {
			log.warn(definitionName + ":" + e, e);
			return new FailedResult(pid, branch, version, e, null);
		}
	}
}
