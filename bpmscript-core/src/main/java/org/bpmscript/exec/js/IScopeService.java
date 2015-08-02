package org.bpmscript.exec.js;

import java.io.IOException;
import java.util.Map;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IProcessDefinition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * The service used for creating and looking up the appropriate scopes for services.
 */
public interface IScopeService {
    
    /**
     * Creates a scope based on the process definition.
     * 
     * @param definitionId the id of the definition
     * @param processDefinition the process definition
     * @param invocationContext any context objects (e.g. scriptchannel and log)
     * @param cx the Javascript Context Object
     * @return a top level scope
     * @throws IOException if there was a problem reading streams / files etc.
     * @throws BpmScriptException if there was any other problem
     */
    Scriptable createScope(String definitionId, IProcessDefinition processDefinition,
            Map<String, Object> invocationContext, Context cx) throws IOException,
            BpmScriptException;
    
    /**
     * Finds a scope for particular definitionId, adds any context objects to the
     * result.
     *  
     * @param definitionId the id of the definition, used to look up the right scope
     * @param invocationContext any context objects (which ware added to the result)
     * @param cx the Javascript context
     * @return a top level scope
     * @throws IOException if there was a problem reading any files
     * @throws BpmScriptException if something goes wrong
     */
    Scriptable findScope(String definitionId,
            Map<String, Object> invocationContext, Context cx) throws IOException,
            BpmScriptException;
}
