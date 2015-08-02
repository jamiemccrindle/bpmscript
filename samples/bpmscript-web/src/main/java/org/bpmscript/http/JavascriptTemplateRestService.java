package org.bpmscript.http;

import java.util.Map;

import org.bpmscript.BpmScriptException;
import org.bpmscript.integration.internal.JsConverter;
import org.mozilla.javascript.Scriptable;

public class JavascriptTemplateRestService {
    
    private TemplateRestService templateRestService;
    private JsConverter jsConverter = new JsConverter();
    
    @SuppressWarnings("unchecked")
    public String post(String url, String template, Scriptable content) throws BpmScriptException {
        return templateRestService.post(url, template, (Map<String, Object>) jsConverter.convert(content));
    }

    public void setTemplateRestService(TemplateRestService templateRestService) {
        this.templateRestService = templateRestService;
    }

    public void setJsConverter(JsConverter jsConverter) {
        this.jsConverter = jsConverter;
    }
}
