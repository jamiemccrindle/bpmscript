/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bpmscript.exec.js;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.RhinoException;

/**
 * Parses a rhino error to get the Javascript exception stack
 * 
	at org.mozilla.javascript.Interpreter.interpretLoop(Interpreter.java:2460)
	at script.pause(classpath:/org/bpmscript/jbi/bpmscriptlibrary.js:2)
	at script(classpath:/org/bpmscript/jbi/bpmscriptlibrary.js:86)
	at script.main(main.js:129)
	at org.mozilla.javascript.Interpreter.interpret(Interpreter.java:2250)
	at org.mozilla.javascript.InterpretedFunction.call(InterpretedFunction.java:149)
	at org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:337)
	at org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:2755)
	at org.mozilla.javascript.InterpretedFunction.call(InterpretedFunction.java:147)
	at org.bpmscript.jbi.ProcessExecutor.send(JavascriptProcessExecutor.java:91)
	at org.bpmscript.jbi.BpmScriptComponent.onMessageExchange(BpmScriptComponent.java:77)
	at org.apache.servicemix.jbi.messaging.DeliveryChannelImpl.processInBound(DeliveryChannelImpl.java:671)
	at org.apache.servicemix.jbi.nmr.flow.AbstractFlow.doRouting(AbstractFlow.java:176)
	at org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow.access$001(JMSFlow.java:68)
	at org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow$1.run(JMSFlow.java:411)
	at org.apache.geronimo.connector.work.WorkerContext.run(WorkerContext.java:291)
	at EDU.oswego.cs.dl.util.concurrent.PooledExecutor$Worker.run(Unknown Source)
	at java.lang.Thread.run(Unknown Source)
 */
public class StackTraceParser {
	
	Pattern pattern = Pattern.compile("at script(?:\\.(.*?))?\\((.*):(.*?)\\)");
	
	/**
	 * @param ex the rhino exception to interrogate
	 * @return the Javascript stack trace elements
	 */
	public StackTraceElement[]	getScriptStackTrace(RhinoException ex) {
		StringWriter st = new StringWriter();
		ex.printStackTrace(new PrintWriter(st));
		String trace = st.toString();
		return getScriptStackTrace(trace);
	}
	
	/**
	 * @param trace the stack trace as a string
	 * @return the Javascript stack trace elements
	 */
	public StackTraceElement[]	getScriptStackTrace(String trace) {
		List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
		BufferedReader reader = new BufferedReader(new StringReader(trace));
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if(matcher.find()) {
					String method = matcher.group(1);
					String name = matcher.group(2);
					int lineNumber = Integer.parseInt(matcher.group(3));
					StackTraceElement element = new StackTraceElement("script", method != null ? method : "", name, lineNumber);
					elements.add(element);
				}
			}
		} catch (IOException e) {
			// this should really not happen
			throw new RuntimeException(e);
		}
		return (StackTraceElement[]) elements.toArray(new StackTraceElement[elements.size()]);
	}
}
