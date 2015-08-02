package org.bpmscript.http;

import java.util.Map;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ITemplateService;

public class TemplateRestService {

    private ITemplateService templateService;
    private IRestService restService;

    public void setRestService(IRestService restService) {
        this.restService = restService;
    }

    public String post(String url, String template, Map<String, Object> content) throws BpmScriptException {
        String result = templateService.merge(template, content);
        return restService.post(url, result);
    }

    public void setTemplateService(ITemplateService templateService) {
        this.templateService = templateService;
    }

}
