package org.bpmscript.integration.internal;

import org.bpmscript.exec.java.IJavaProcessDefinition;
import org.bpmscript.exec.java.JavaProcessExecutor;
import org.bpmscript.exec.java.seriaize.JavaSerializingContinuationService;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.exec.js.JavascriptProcessExecutor;
import org.bpmscript.exec.js.scope.ScopeService;
import org.bpmscript.exec.js.scope.memory.MemoryScopeStore;
import org.bpmscript.exec.js.serialize.JavascriptSerializingContinuationService;
import org.bpmscript.integration.internal.channel.InternalScriptChannel;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.ProcessExecutorLookup;
import org.bpmscript.process.memory.MemoryBpmScriptManager;
import org.bpmscript.process.memory.MemoryDefinitionManager;
import org.bpmscript.process.memory.MemoryVersionedDefinitionManager;
import org.bpmscript.test.IServiceLookup;
import org.bpmscript.test.ITestCallback;
import org.bpmscript.test.ITestSupport;
import org.bpmscript.test.ServiceLookup;

public class MemoryEngineTestSupport implements ITestSupport<IServiceLookup> {

    private IMessageSender sender;
    
    public MemoryEngineTestSupport() {}
    
    public MemoryEngineTestSupport(IMessageSender sender) {
        super();
        this.sender = sender;
    }
    /* (non-Javadoc)
     * @see org.bpmscript.test.ITestSupport#execute(org.bpmscript.test.ITestCallback)
     */
    public void execute(ITestCallback<IServiceLookup> callback) throws Exception {
        
        final MemoryVersionedDefinitionManager processManager = new MemoryVersionedDefinitionManager();
        final MemoryBpmScriptManager bpmScriptManager = new MemoryBpmScriptManager();
        processManager.setDefinitionManager(new MemoryDefinitionManager());
        InternalScriptChannel scriptChannel = new InternalScriptChannel();
        scriptChannel.setReplyTo("bpmscript-next");
        scriptChannel.setSender(sender);
        scriptChannel.setTimeoutAddress("timeout");
        scriptChannel.setErrorAddress("error");
                
        // javascript executor
        JavascriptProcessExecutor javascriptExecutor = new JavascriptProcessExecutor();
        JavascriptSerializingContinuationService javascriptContinuationService = new JavascriptSerializingContinuationService();
        javascriptContinuationService.setContinuationStore(bpmScriptManager);
        javascriptExecutor.setContinuationService(javascriptContinuationService);
        MemoryScopeStore scopeStore = new MemoryScopeStore();
        ScopeService scopeService = new ScopeService();
        scopeService.setScopeStore(scopeStore);
        
        javascriptExecutor.setScopeService(scopeService);
        javascriptExecutor.setChannel(scriptChannel);
        
        // java executor
        JavaProcessExecutor javaExecutor = new JavaProcessExecutor();
        JavaSerializingContinuationService javaContinuationService = new JavaSerializingContinuationService();
        javaContinuationService.setContinuationJournal(bpmScriptManager);
        javaExecutor.setContinuationService(javaContinuationService);
        javaExecutor.setChannel(scriptChannel);

        final BpmScriptEngine engine = new BpmScriptEngine();
        engine.setInstanceManager(bpmScriptManager);
        ProcessExecutorLookup processExecutorLookup = new ProcessExecutorLookup();
        processExecutorLookup.addProcessExecutor(IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT, javascriptExecutor);
        processExecutorLookup.addProcessExecutor(IJavaProcessDefinition.DEFINITION_TYPE_JAVA, javaExecutor);
        engine.setProcessExecutorLookup(processExecutorLookup);
        engine.setContinuationJournal(bpmScriptManager);
        engine.setVersionedDefinitionManager(processManager);
        
        ServiceLookup serviceLookup = new ServiceLookup();
        serviceLookup.addService("engine", engine);
        serviceLookup.addService("continuationJournal", bpmScriptManager);
        serviceLookup.addService("continuationService", javascriptContinuationService);
        serviceLookup.addService("scopeStore", scopeStore);
        serviceLookup.addService("instanceManager", engine);
        serviceLookup.addService("versionedDefinitionManager", engine);

        try {
            callback.execute(serviceLookup);
        } finally {
            // any close code
        }
        
    }
    public IMessageSender getSender() {
        return sender;
    }

    public void setSender(IMessageSender sender) {
        this.sender = sender;
    }

}
