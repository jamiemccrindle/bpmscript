package org.bpmscript;

import java.util.Map;

/**
 * A service that takes in a map of parameters and a template name and returns
 * a string with the template parameters merged into the template.
 */
public interface ITemplateService {
    
    /**
     * Merge the content template parameters into the named template and return
     * the result as a string.
     * 
     * @param template the template name
     * @param content the parameters to merge into the template
     * @return the merged result
     * @throws BpmScriptException if the parameters couldn't be merged into the template.
     */
    public String merge(String template, Map<String, Object> content) throws BpmScriptException;
}
