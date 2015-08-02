package org.bpmscript.process;

import java.util.List;

import junit.framework.TestCase;

import org.bpmscript.IExecutorResult;
import org.bpmscript.exec.CompletedResult;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.exec.js.JavascriptProcessDefinition;
import org.bpmscript.process.memory.MemoryDefinitionManager;
import org.bpmscript.process.memory.MemoryInstanceManager;
import org.bpmscript.util.StreamService;

public class MemoryProcessTest extends TestCase {

	public void testProcessing() throws Throwable {
		MemoryDefinitionManager processManager = new MemoryDefinitionManager();
		String definitionId = "asdf";
		processManager.createDefinition(definitionId, new JavascriptProcessDefinition("test", StreamService.DEFAULT_INSTANCE.getResourceAsString("/org/bpmscript/exec/main.js")));
		assertNotNull(definitionId);
		IDefinition definition = processManager.getDefinition(definitionId);
		assertNotNull(definition);
		assertEquals("test", definition.getName());
		List<IDefinition> definitionsByName = processManager.getDefinitionsByName("test");
		assertEquals(1, definitionsByName.size());
		MemoryInstanceManager instanceManager = new MemoryInstanceManager();
		final String pid = instanceManager.createInstance(null, definitionId, "test", IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, "test");
		assertNotNull(pid);
		IInstance instance = instanceManager.getInstance(pid);
		assertNotNull(instance);
		assertEquals(definitionId, instance.getDefinitionId());
		assertEquals(pid, instance.getId());
		assertNull(instance.getParentVersion());
		IExecutorResult result = (IExecutorResult) instanceManager.doWithInstance(pid, new IInstanceCallback() {
			public IExecutorResult execute(IInstance processInstance) throws Exception {
				return new CompletedResult(pid, null, null, null);
			}
		});
		assertNotNull(result);
	}

}
