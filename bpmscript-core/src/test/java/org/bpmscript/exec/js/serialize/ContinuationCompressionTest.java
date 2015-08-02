package org.bpmscript.exec.js.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.collections.MultiMap;
import org.bpmscript.exec.ContinuationError;
import org.bpmscript.js.Global;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.continuations.Continuation;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

public class ContinuationCompressionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	@SuppressWarnings("unchecked")
    public void testWriting() throws Exception {
        Context context = new ContextFactory().enterContext();
        context.setOptimizationLevel(-1);
        Scriptable scope = getScope(context);

        MonitoringOutputStream m1 = new MonitoringOutputStream(new ByteArrayOutputStream());
        try {
            Object[] ids = scope.getIds();
            Object[] objects = new Object[ids.length];
            for (int i = 0; i < ids.length; i++) {
                Object object = ids[i];
                objects[i] = scope.get(object.toString(), scope);
            }
            Function function = (Function) scope.get("serialize", scope);
            function.call(context, scope, scope, new Object[] {objects});
        } catch(ContinuationError e) {
            Continuation continuation = e.getContinuation();
            ScriptableOutputStream scriptableOutputStream = 
                new ScriptableOutputStream(
                        m1, 
                        ScriptableObject.getTopLevelScope(continuation));
            scriptableOutputStream.writeObject(continuation);
        }
        
        byte[] bytes = null;
        Continuation continuation = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Function function = (Function) scope.get("one", scope);
            function.call(context, scope, scope, new Object[] {});
        } catch(ContinuationError e) {
            continuation = e.getContinuation();
            ScriptableOutputStream scriptableOutputStream = 
                new ScriptableOutputStream(
                        out, 
                        ScriptableObject.getTopLevelScope(continuation));
            scriptableOutputStream.writeObject(continuation);
            bytes = out.toByteArray();
        }
        
        for(int i = 0; i < 10; i++) {
            try {
                continuation = getContinuation(bytes, scope);
                Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
                continuation.call(context, continuationScope, continuationScope, new Object[] {});
            } catch(ContinuationError e) {
                continuation = e.getContinuation();
                bytes = getBytes(continuation);
            }
        }
        
        MonitoringOutputStream m2 = new MonitoringOutputStream(out);
        try {
            continuation = getContinuation(bytes, scope);
            Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
            continuation.call(context, continuationScope, continuationScope, new Object[] {});
        } catch(ContinuationError e) {
            continuation = e.getContinuation();
            ScriptableOutputStream scriptableOutputStream = 
                new ScriptableOutputStream(
                        m2, 
                        ScriptableObject.getTopLevelScope(continuation));
            scriptableOutputStream.writeObject(continuation);
            bytes = out.toByteArray();
        }
        
        MultiMap bytesRun1 = m1.getBytesRun();
        MultiMap bytesRun2 = m2.getBytesRun();
        
//        for (Iterator iterator = bytesRun1.entrySet().iterator(); iterator.hasNext();) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            int size = ((Collection) entry.getValue()).size();
//            if(size > 1) {
//                System.out.println(size + " " + entry.getKey());
//            }
//        }
//        
        int beforeSize = 0;
        for (Iterator iterator = bytesRun2.keySet().iterator(); iterator.hasNext();) {
            BytesRun bytesRun = (BytesRun) iterator.next();
            beforeSize += bytesRun.bytes.length;
        }
        
        System.out.println(beforeSize);
        
        bytesRun2.keySet().removeAll(bytesRun1.keySet());

        int afterSize = 0;
        for (Iterator iterator = bytesRun2.keySet().iterator(); iterator.hasNext();) {
            BytesRun bytesRun = (BytesRun) iterator.next();
            afterSize += bytesRun.bytes.length;
        }

        System.out.println(afterSize);

        Set keySet = bytesRun2.keySet();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
            BytesRun bytesRun = (BytesRun) iterator.next();
            assertNotNull(bytesRun);
            //System.out.println(new String(bytesRun.bytes));
        }
        
    }

//	public void testSerialization() throws Exception {
//		Context context = new ContextFactory().enterContext();
//		context.setOptimizationLevel(-1);
//		try {
//			Continuation continuation = null;
//			byte[] bytes = null;
//			try {
//				Scriptable scope = getScope(context);
//				Function function = (Function) scope.get("one", scope);
//				function.call(context, scope, scope, new Object[] {});
//			} catch(ContinuationError e) {
//				continuation = e.getContinuation();
//				bytes = getBytes(continuation);
//			}
//            try {
//                Scriptable scope = getScope(context);
//                continuation = getContinuation(bytes, scope);
//                Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
//                continuation.call(context, continuationScope, continuationScope, new Object[] {});
//            } catch(ContinuationError e) {
//                continuation = e.getContinuation();
//                bytes = getBytes(continuation);
//            }
//            try {
//                Scriptable scope = getScope(context);
//                continuation = getContinuation(bytes, scope);
//                Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);
//                continuation.call(context, continuationScope, continuationScope, new Object[] {});
//            } catch(ContinuationError e) {
//                continuation = e.getContinuation();
//                bytes = getBytes(continuation);
//            }
//		} finally {
//			Context.exit();
//		}
//	}

    private byte[] getBytes(Continuation continuation) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Scriptable continuationScope = ScriptableObject.getTopLevelScope(continuation);

        ScriptableOutputStream scriptableOutputStream = new ScriptableOutputStream(out, continuationScope);
        scriptableOutputStream.writeObject(continuation);
        
        byte[] bs = out.toByteArray();
        return bs;
    }

    private Continuation getContinuation(byte[] bytes, Scriptable scope) throws IOException, ClassNotFoundException {
        ScriptableInputStream scriptableInputStream = new ScriptableInputStream(new ByteArrayInputStream(bytes), scope);
        Continuation continuation = (Continuation) scriptableInputStream.readObject();
        return continuation;
    }

	private Scriptable getScope(Context context) throws IOException {
		Global scope = new Global(context);
		Script script = context.compileReader(new InputStreamReader(getClass()
				.getResourceAsStream(
						"/org/bpmscript/exec/serialize/bigger.js")),
				"/org/bpmscript/exec/serialize/bigger.js", 0, null);
		script.exec(context, scope);
		return scope;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
