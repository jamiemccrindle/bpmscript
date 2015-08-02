package org.bpmscript.exec.js.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import junit.framework.TestCase;

import org.bpmscript.exec.ContinuationError;
import org.bpmscript.exec.js.serialize.FunctionStubService;
import org.bpmscript.exec.js.serialize.ObjectStubService;
import org.bpmscript.exec.js.serialize.StubService;
import org.bpmscript.exec.js.serialize.StubbedInputStream;
import org.bpmscript.exec.js.serialize.StubbedOutputStream;
import org.bpmscript.js.Global;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.continuations.Continuation;

public class ContinuationSerializationTest extends TestCase {

	private FunctionStubService functionStubService = new FunctionStubService();
	private ObjectStubService objectStubService = new ObjectStubService();

	protected void setUp() throws Exception {
		super.setUp();
	}
	
//    try {
//        Scriptable scope = getScope(context);
//        Function function = (Function) scope.get("serialize", scope);
//        function.call(context, scope, scope, new Object[] {null});
//    } catch(ContinuationError e) {
//        continuation = e.getContinuation();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        ScriptableOutputStream scriptableOutputStream = 
//            new ScriptableOutputStream(
//                    out, 
//                    ScriptableObject.getTopLevelScope(continuation));
//        scriptableOutputStream.writeObject(continuation);
//        bytes = getBytes(continuation);
//        continuation = getContinuation(bytes, ScriptableObject.getTopLevelScope(continuation));
//        Scriptable topLevelScope = ScriptableObject.getTopLevelScope(continuation);
//        System.out.println(topLevelScope);
//    }

	public void testSerialization() throws Exception {
		Context context = new ContextFactory().enterContext();
		context.setOptimizationLevel(-1);
		try {
			Continuation continuation = null;
			byte[] bytes = null;
			try {
				Scriptable scope = getScope(context);
				Function function = (Function) scope.get("one", scope);
				function.call(context, scope, scope, new Object[] {});
			} catch(ContinuationError e) {
				continuation = e.getContinuation();
				bytes = getBytes(continuation);
			}
            try {
                Scriptable scope = getScope(context);
                continuation = getContinuation(bytes, scope);
                Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
                continuation.call(context, continuationScope, continuationScope, new Object[] {});
            } catch(ContinuationError e) {
                continuation = e.getContinuation();
                bytes = getBytes(continuation);
            }
            try {
                Scriptable scope = getScope(context);
                continuation = getContinuation(bytes, scope);
                Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
                continuation.call(context, continuationScope, continuationScope, new Object[] {});
            } catch(ContinuationError e) {
                continuation = e.getContinuation();
                bytes = getBytes(continuation);
            }
		} finally {
			Context.exit();
		}
	}

    private byte[] getBytes(Continuation continuation) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
        
        StubbedOutputStream stubbedOutputStream = 
            new StubbedOutputStream(
                    out, 
                    continuationScope, 
                    new StubService(
                            functionStubService.getStubs(continuationScope),
                            objectStubService.getStubs(new HashMap<String, Object>())));
        
        stubbedOutputStream.writeObject(continuation);
        
        byte[] bs = out.toByteArray();
        return bs;
    }

    private Continuation getContinuation(byte[] bytes, Scriptable scope) throws IOException, ClassNotFoundException {
        StubbedInputStream stubbedInputStream = 
            new StubbedInputStream(
                    new ByteArrayInputStream(bytes), 
                    scope, 
                    new StubService(
                            functionStubService.getStubs(scope),
                            objectStubService.getStubs(new HashMap<String, Object>())));
        Continuation continuation = (Continuation) stubbedInputStream.readObject();
        return continuation;
    }

	private Scriptable getScope(Context context) throws IOException {
		Global scope = new Global(context);
		Script script = context.compileReader(new InputStreamReader(getClass()
				.getResourceAsStream(
						"/org/bpmscript/exec/serialize/main.js")),
				"/org/bpmscript/exec/serialize/main.js", 0, null);
		script.exec(context, scope);
		return scope;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
