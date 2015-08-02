package org.bpmscript.js;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;

import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.js.reload.LibraryToFile;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

/**
 * A Javascript scope used by BpmScript. Adds support for the "require" method which
 * includes other scripts in the current script. It also supports notifying listeners
 * that libraries were loaded for a script.
 */
public class Global extends ImporterTopLevel {
    
	/**
     * Name of the source stack in the thread local for global
     */
    public static final String SOURCE_STACK = "sourceStack";
    public static final String LIBRARY_ASSOCIATION_QUEUE = "libraryAssociationListeners";

    private static final long serialVersionUID = -1803365132384810485L;

	private boolean sealedStdLib = false;

	public Global() {
		super();
	}

	public Global(Context cx) {
		super();
		init(cx);
	}

	public void init(ContextFactory factory) {
		factory.call(new ContextAction() {
			public Object run(Context cx) {
				init(cx);
				return null;
			}
		});
	}

	public void init(Context cx) {
		initStandardObjects(cx, sealedStdLib);
		String[] names = { "require", "inspect", "toJSON", "topLevelScope" };
		defineFunctionProperties(names, Global.class, ScriptableObject.DONTENUM);
	}

    public Object toJSON(Object object) throws IOException {
        return JSONPrinter.DEFAULT_INSTANCE.write(object);
    }
    
    public static Object topLevelScope(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws IOException {
         Scriptable topLevelScope = ScriptableObject.getTopLevelScope(thisObj);
         return topLevelScope;
    }

    public static Object inspect(Context cx, Scriptable thisObj, Object[] args,
            Function funObj) throws IOException {
         return args;
    }

    @SuppressWarnings("unchecked")
	public static Object require(Context cx, Scriptable thisObject, Object[] args,
			Function funObj) throws IOException {
        Scriptable topLevelScope = ScriptableObject.getTopLevelScope(thisObject);
        
        Stack sourceStack = (Stack) cx.getThreadLocal(SOURCE_STACK);
        
        String scriptName = null;
        Object scriptNameObject = args[0];
        if(scriptNameObject instanceof String) {
            scriptName = (String) scriptNameObject;
        } else if (scriptNameObject instanceof NativeJavaObject) {
            Object unwrap = ((NativeJavaObject) scriptNameObject).unwrap();
            if(unwrap instanceof String) {
                scriptName = (String) unwrap;
            }
        }
        
        if(scriptName == null) {
            throw new RuntimeException("Could not determine the library to load");
        }
        
		Scriptable scope = topLevelScope;
		// check to see whether this package should be in the global scope or a custom scope
		if(args.length > 1) {
		    String packageScope = (String) args[1];
		    scope = new NativeObject();
		    scope.setParentScope(topLevelScope);
		    ScriptableObject.putProperty(topLevelScope, packageScope, scope);
		}
		Object importedScriptsObject = topLevelScope.get("__importedScripts__", topLevelScope);
		Set<String> importedScripts = null;
		if(importedScriptsObject instanceof UniqueTag) {
			importedScripts = new HashSet<String>();
			topLevelScope.put("__importedScripts__", topLevelScope, importedScripts);
		} else {
			importedScripts = (Set<String>) importedScriptsObject;
		}
		if(!importedScripts.contains(scriptName)) {
			InputStream resourceAsStream = topLevelScope.getClass().getResourceAsStream(scriptName); 
			if(resourceAsStream == null) {
				throw new IOException(scriptName + " could not be found on the classpath");
			}
			Script script = cx.compileReader(new InputStreamReader(resourceAsStream), scriptName, 1, null);
			if(sourceStack != null) { sourceStack.push(scriptName); }
			try {
			    script.exec(cx, scope);
			} finally {
			    if(sourceStack != null) { sourceStack.pop(); }
			}
			importedScripts.add(scriptName);
		}
        // not great using thread locals but what can I do? I ask you dear reader...
        BlockingQueue<ILibraryToFile> libraryAssociationQueue = (BlockingQueue<ILibraryToFile>) cx.getThreadLocal(LIBRARY_ASSOCIATION_QUEUE);
        if(libraryAssociationQueue != null && sourceStack != null) {
            libraryAssociationQueue.add(new LibraryToFile(scriptName, (String) sourceStack.peek()));
        }
		return null;
	}
}
