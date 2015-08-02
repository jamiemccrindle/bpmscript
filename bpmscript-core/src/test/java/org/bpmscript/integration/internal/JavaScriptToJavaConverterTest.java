package org.bpmscript.integration.internal;

import java.io.InputStreamReader;
import java.util.Map;

import junit.framework.TestCase;

import org.bpmscript.integration.internal.JsConverter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptToJavaConverterTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	@SuppressWarnings("unchecked")
	public void testConverter() throws Exception {
		JsConverter converter = new JsConverter();
		Context cx = new ContextFactory().enterContext();
		try {
			ScriptableObject scope = cx.initStandardObjects();
			String scriptFilename = "/org/bpmscript/channel/converter/convert1.js";
			Script script = cx.compileReader(new InputStreamReader(
					getClass().getResourceAsStream(scriptFilename)), scriptFilename, 0, null);
			Object object = script.exec(cx, scope);
			Object result = converter.convert(object);
			assertTrue(result instanceof Map);
			Map<String, Object> map = (Map<String, Object>) result;
			Object argumentsObject = map.get("arguments");
			assertTrue(argumentsObject instanceof Object[]);
		} finally {
			Context.exit();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
