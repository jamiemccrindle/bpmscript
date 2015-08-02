package org.bpmscript.js;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ITemplateService;
import org.bpmscript.js.reload.ILibraryChangeListener;
import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.util.StreamService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class JavascriptTemplateService implements ResourceLoaderAware, ITemplateService, ILibraryChangeListener {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private IJavascriptSourceCache javascriptSourceCache;
    private String prefix;
    private String suffix;
    private ResourceLoader resourceLoader;

    private String configResource;
    private MapToJsConverter mapToJsConverter = MapToJsConverter.DEFAULT_INSTANCE;
    private BlockingQueue<ILibraryToFile> libraryAssociationQueue; 

    public String merge(String template, Map<String, Object> content) throws BpmScriptException {
        String fullTemplateName = prefix + template + suffix;
        try {
            Context cx = new DynamicContextFactory().enterContext();
            try {
                Global scope = new Global(cx);
                cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraryAssociationQueue);
                ScriptableObject.putProperty(scope, "log", Context.javaToJS(log, scope));
                Set<Entry<String, Object>> entrySet = content.entrySet();
                for (Map.Entry<String, Object> entry : entrySet) {
                    ScriptableObject.putProperty(scope, entry.getKey(), mapToJsConverter.convertObject(scope, entry.getValue()));
                }
                Stack<String> sourceStack = new Stack<String>();
                if(libraryAssociationQueue != null) {
                    cx.putThreadLocal(Global.LIBRARY_ASSOCIATION_QUEUE, libraryAssociationQueue);
                }
                if(configResource != null) {
                    sourceStack.push(configResource);
                    cx.putThreadLocal(Global.SOURCE_STACK, sourceStack);
                    Script configScript = javascriptSourceCache.getScript(
                            StreamService.DEFAULT_INSTANCE.readFully(resourceLoader.getResource(configResource).getInputStream()), configResource);
                    configScript.exec(cx, scope);
                    sourceStack.pop();
                }
                
                sourceStack.push(fullTemplateName);
                Script script = javascriptSourceCache.getScript(
                        StreamService.DEFAULT_INSTANCE.readFully(resourceLoader.getResource(fullTemplateName).getInputStream()), fullTemplateName);
                Object result = script.exec(cx, scope);
                return result.toString();
            } finally {
                Context.exit();
            }
        } catch (IOException e) {
            throw new BpmScriptException(e);
        }
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    public void setJavascriptSourceCache(IJavascriptSourceCache javascriptSourceCache) {
        this.javascriptSourceCache = javascriptSourceCache;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setConfigResource(String configResource) {
        this.configResource = configResource;
    }

    public void setMapToJsConverter(MapToJsConverter mapToJsConverter) {
        this.mapToJsConverter = mapToJsConverter;
    }

    public void setLibraryAssociationQueue(BlockingQueue<ILibraryToFile> libraryAssociationQueue) {
        this.libraryAssociationQueue = libraryAssociationQueue;
    }

    /**
     * Does nothing for now...
     */
    public void onLibraryChange(String library, String file) {
        // do we do any caching? we probably should...
    }
}
