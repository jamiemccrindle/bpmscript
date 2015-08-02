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

package org.bpmscript.process.memory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bpmscript.BpmScriptException;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.ProcessState;
import org.bpmscript.correlation.IConversationCorrelator;
import org.bpmscript.exec.java.IJavaProcessDefinition;
import org.bpmscript.exec.java.JavaProcessExecutor;
import org.bpmscript.exec.java.seriaize.JavaSerializingContinuationService;
import org.bpmscript.exec.js.IJavascriptProcessDefinition;
import org.bpmscript.exec.js.JavascriptProcessExecutor;
import org.bpmscript.exec.js.scope.ScopeService;
import org.bpmscript.exec.js.scope.memory.MemoryScopeStore;
import org.bpmscript.exec.js.serialize.JavascriptSerializingContinuationService;
import org.bpmscript.integration.IMessageBus;
import org.bpmscript.integration.internal.IInternalMessage;
import org.bpmscript.integration.internal.IMessageSender;
import org.bpmscript.integration.internal.adapter.BpmScriptFirstAdapter;
import org.bpmscript.integration.internal.adapter.BpmScriptNextAdapter;
import org.bpmscript.integration.internal.adapter.ConversationCorrelatorAdapter;
import org.bpmscript.integration.internal.adapter.LoggingAdapter;
import org.bpmscript.integration.internal.adapter.SpringAdapter;
import org.bpmscript.integration.internal.adapter.TimeoutAdapter;
import org.bpmscript.integration.internal.channel.InternalScriptChannel;
import org.bpmscript.integration.internal.correlation.ConversationCorrelator;
import org.bpmscript.integration.internal.memory.MemorySyncChannel;
import org.bpmscript.integration.internal.proxy.BpmScriptFacade;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.journal.IContinuationJournalEntry;
import org.bpmscript.js.reload.ILibraryToFile;
import org.bpmscript.js.reload.LibraryFileMonitor;
import org.bpmscript.paging.IPagedResult;
import org.bpmscript.paging.IQuery;
import org.bpmscript.paging.PagedResult;
import org.bpmscript.process.BpmScriptEngine;
import org.bpmscript.process.FolderDefinitionLoader;
import org.bpmscript.process.IAutoStartManager;
import org.bpmscript.process.IBpmScriptEngine;
import org.bpmscript.process.IBpmScriptFacade;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IDefinitionManager;
import org.bpmscript.process.IInstance;
import org.bpmscript.process.IInstanceCallback;
import org.bpmscript.process.IInstanceKiller;
import org.bpmscript.process.IInstanceListener;
import org.bpmscript.process.IInstanceManager;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.process.ProcessExecutorLookup;
import org.bpmscript.process.autostart.AutoStartManager;
import org.bpmscript.process.kill.InstanceKiller;
import org.bpmscript.process.spring.ApplicationContextDefinitionConfigurationLookup;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 */
public class MemoryBpmScriptEngine implements InitializingBean, DisposableBean, ApplicationContextAware, IBpmScriptEngine,
        IVersionedDefinitionManager, IInstanceManager, IContinuationJournal, IMessageSender, IBpmScriptFacade, IConversationCorrelator, IAutoStartManager, IInstanceKiller {

    private ApplicationContext applicationContext;
    private MemoryVersionedDefinitionManager versionedDefinitionManager;
    private MemoryBpmScriptManager bpmScriptManager;
    private BpmScriptEngine engine;
    private String bpmScriptNextChannelName = "bpmscript-next";
    private String timeoutChannelName = "timeout";
    private String errorChannelName = "error";
    private String springChannelName = "spring";
    private String bpmScriptFirstChannelName = "bpmscript-first";
    private String syncChannelName = "sync";
    private String conversationChannelName = "conversation";
    private IMessageBus bus;
    private ConversationCorrelator conversationCorrelator;
    private Resource folder;
    private BpmScriptFacade bpmScriptFacade;
    private InstanceKiller instanceKiller;
    /**
     * @param name
     * @param autoKill
     * @see org.bpmscript.process.autostart.AutoStartManager#startup(java.lang.String, boolean)
     */
    public void startup(String name, boolean autoKill) {
        autoStartManager.startup(name, autoKill);
    }

    /**
     * @param definitionId
     * @param message
     * @return
     * @throws BpmScriptException
     * @see org.bpmscript.process.kill.InstanceKiller#killDefinitionInstances(java.lang.String, java.lang.String)
     */
    public int killDefinitionInstances(String definitionId, String message)
            throws BpmScriptException {
        return instanceKiller.killDefinitionInstances(definitionId, message);
    }

    /**
     * @param pid
     * @param message
     * @throws BpmScriptException
     * @see org.bpmscript.process.kill.InstanceKiller#killInstance(java.lang.String, java.lang.String)
     */
    public void killInstance(String pid, String message)
            throws BpmScriptException {
        instanceKiller.killInstance(pid, message);
    }

    private AutoStartManager autoStartManager;

    public MemoryBpmScriptEngine() {

    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        
        Assert.notNull(folder, "The folder property must be set");
        Assert.notNull(bus, "The bus property must be set");

        BlockingQueue<ILibraryToFile> libraryAssociationQueue = new LinkedBlockingQueue<ILibraryToFile>();
        BlockingQueue<ILibraryToFile> libraryChangeQueue = new LinkedBlockingQueue<ILibraryToFile>();
        
        versionedDefinitionManager = new MemoryVersionedDefinitionManager();
        bpmScriptManager = new MemoryBpmScriptManager();
        versionedDefinitionManager.setDefinitionManager(new MemoryDefinitionManager());
        InternalScriptChannel scriptChannel = new InternalScriptChannel();
        scriptChannel.setReplyTo(bus.getName() + ":" + bpmScriptNextChannelName);
        scriptChannel.setSender(bus);
        scriptChannel.setTimeoutAddress(bus.getName() + ":" + timeoutChannelName);
        scriptChannel.setErrorAddress(bus.getName() + ":" + errorChannelName);
        scriptChannel.setDefinitionConfigurationLookup(new ApplicationContextDefinitionConfigurationLookup(
                applicationContext));

        // javascript executor
        JavascriptProcessExecutor javascriptExecutor = new JavascriptProcessExecutor();
        JavascriptSerializingContinuationService javascriptContinuationService = new JavascriptSerializingContinuationService();
        javascriptContinuationService.setContinuationStore(bpmScriptManager);
        javascriptExecutor.setContinuationService(javascriptContinuationService);
        MemoryScopeStore scopeStore = new MemoryScopeStore();
        ScopeService scopeService = new ScopeService();
        scopeService.setLibraryAssociationQueue(libraryAssociationQueue);
        scopeService.setScopeStore(scopeStore);

        javascriptExecutor.setScopeService(scopeService);
        javascriptExecutor.setChannel(scriptChannel);

        // java executor
        JavaProcessExecutor javaExecutor = new JavaProcessExecutor();
        JavaSerializingContinuationService javaContinuationService = new JavaSerializingContinuationService();
        javaContinuationService.setContinuationJournal(bpmScriptManager);
        javaExecutor.setContinuationService(javaContinuationService);
        javaExecutor.setChannel(scriptChannel);

        engine = new BpmScriptEngine();
        engine.setInstanceManager(bpmScriptManager);
        ProcessExecutorLookup processExecutorLookup = new ProcessExecutorLookup();
        processExecutorLookup.addProcessExecutor(IJavascriptProcessDefinition.DEFINITION_TYPE_JAVASCRIPT,
                javascriptExecutor);
        processExecutorLookup.addProcessExecutor(IJavaProcessDefinition.DEFINITION_TYPE_JAVA, javaExecutor);
        engine.setProcessExecutorLookup(processExecutorLookup);
        engine.setContinuationJournal(bpmScriptManager);
        engine.setVersionedDefinitionManager(versionedDefinitionManager);
        
        MemorySyncChannel syncChannel = new MemorySyncChannel();

        bpmScriptFacade = new BpmScriptFacade();
        bpmScriptFacade.setAddress(bus.getName() + ":" + bpmScriptFirstChannelName);
        bpmScriptFacade.setReplyTo(bus.getName() + ":" + syncChannelName);
        bpmScriptFacade.setSender(bus);
        bpmScriptFacade.setSyncChannel(syncChannel);
        
        conversationCorrelator = new ConversationCorrelator();
        conversationCorrelator.setAddress(bus.getName() + ":" + bpmScriptNextChannelName);
        conversationCorrelator.setCorrelatorAddress(bus.getName() + ":" + conversationChannelName);
        conversationCorrelator.setReplyTo(bus.getName() + ":" + syncChannelName);
        conversationCorrelator.setSender(bus);
        conversationCorrelator.setSyncChannel(syncChannel);

        BpmScriptFirstAdapter bpmScriptFirstAdapter = new BpmScriptFirstAdapter(engine, bus);
        BpmScriptNextAdapter bpmScriptNextAdapter = new BpmScriptNextAdapter(engine, bus, bus.getName() + ":" + errorChannelName);

        TimeoutAdapter timeoutAdapter = new TimeoutAdapter(bus);

        ConversationCorrelatorAdapter conversationCorrelatorAdapter = new ConversationCorrelatorAdapter();
        conversationCorrelatorAdapter.setSender(bus);
        
        SpringAdapter springAdapter = new SpringAdapter(applicationContext, bus);
        bus.register(springChannelName, springAdapter);
        bus.register(bpmScriptFirstChannelName, bpmScriptFirstAdapter);
        bus.register(bpmScriptNextChannelName, bpmScriptNextAdapter);
        bus.register(timeoutChannelName, timeoutAdapter);
        bus.register(errorChannelName, new LoggingAdapter());
        bus.register(syncChannelName, syncChannel);
        bus.register(conversationChannelName, conversationCorrelatorAdapter);
        
        LibraryFileMonitor libraryFileMonitor = new LibraryFileMonitor();
        libraryFileMonitor.setLibraryAssociationQueue(libraryAssociationQueue);
        libraryFileMonitor.setLibraryChangeQueue(libraryChangeQueue);
        
        FolderDefinitionLoader folderDefinitionLoader = new FolderDefinitionLoader();
        folderDefinitionLoader.setEngine(engine);
        folderDefinitionLoader.setFolder(folder);
        folderDefinitionLoader.setLibraryChangeQueue(libraryChangeQueue);
        folderDefinitionLoader.setVersionedDefinitionManager(versionedDefinitionManager);
        folderDefinitionLoader.afterPropertiesSet();
        
        instanceKiller = new InstanceKiller();
        instanceKiller.setBpmScriptEngine(engine);
        instanceKiller.setContinuationJournal(bpmScriptManager);
        instanceKiller.setInstanceStatusManager(bpmScriptManager);
        
        autoStartManager = new AutoStartManager();
        autoStartManager.setBpmScriptFacade(bpmScriptFacade);
        autoStartManager.setInstanceKiller(instanceKiller);
        autoStartManager.setInstanceStatusManager(bpmScriptManager);
        autoStartManager.setVersionedDefinitionManager(versionedDefinitionManager);

    }

    public void setFolder(Resource folder) {
        this.folder = folder;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String createBranch(String version) {
        return bpmScriptManager.createBranch(version);
    }

    public String createInstance(String parentVersion, String definitionId, String definitionName,
            String definitionType, String operation) throws BpmScriptException {
        return bpmScriptManager.createInstance(parentVersion, definitionId, definitionName, definitionType, operation);
    }

    public String createMainBranch(String pid) {
        return bpmScriptManager.createMainBranch(pid);
    }

    public String createVersion(String pid, String branch) {
        return bpmScriptManager.createVersion(pid, branch);
    }

    public IExecutorResult doWithInstance(String processInstanceId, IInstanceCallback callback) throws Exception {
        return bpmScriptManager.doWithInstance(processInstanceId, callback);
    }

    public Collection<String> getBranchesForPid(String pid) {
        return bpmScriptManager.getBranchesForPid(pid);
    }

    public List<IInstance> getChildInstances(String parentVersion) throws BpmScriptException {
        return bpmScriptManager.getChildInstances(parentVersion);
    }

    public byte[] getContinuationLatest(String branch) {
        return bpmScriptManager.getContinuationLatest(branch);
    }

    public Collection<IContinuationJournalEntry> getEntriesForPid(String pid) {
        return bpmScriptManager.getEntriesForPid(pid);
    }

    public IInstance getInstance(String pid) throws BpmScriptException {
        return bpmScriptManager.getInstance(pid);
    }

    public IPagedResult<IInstance> getInstances(IQuery query) throws BpmScriptException {
        return bpmScriptManager.getInstances(query);
    }

    public PagedResult<IInstance> getInstancesForDefinition(IQuery query, String definitionId)
            throws BpmScriptException {
        return bpmScriptManager.getInstancesForDefinition(query, definitionId);
    }

    public Collection<String> getLiveInstances(String definitionId) throws BpmScriptException {
        return bpmScriptManager.getLiveInstances(definitionId);
    }

    public Collection<IContinuationJournalEntry> getLiveResults(String pid) {
        return bpmScriptManager.getLiveResults(pid);
    }

    public ProcessState getProcessStateLatest(String branch) {
        return bpmScriptManager.getProcessStateLatest(branch);
    }

    public String getVersionLatest(String branch) {
        return bpmScriptManager.getVersionLatest(branch);
    }

    public void storeResult(byte[] continuation, IExecutorResult result) {
        bpmScriptManager.storeResult(continuation, result);
    }

    public void kill(String pid, String branch, String message) throws BpmScriptException {
        engine.kill(pid, branch, message);
    }

    public void sendFirst(String parentVersion, String definitionName, String operation, Object message)
            throws BpmScriptException {
        engine.sendFirst(parentVersion, definitionName, operation, message);
    }

    public void sendNext(String pid, String branch, Object message, String queueId) throws BpmScriptException {
        engine.sendNext(pid, branch, message, queueId);
    }

    public void setInstanceListener(IInstanceListener instanceListener) {
        engine.setInstanceListener(instanceListener);
    }

    public String validate(IProcessDefinition processDefinition) throws BpmScriptException {
        return engine.validate(processDefinition);
    }

    public void createDefinition(String id, IProcessDefinition processDefinition) throws BpmScriptException {
        versionedDefinitionManager.createDefinition(id, processDefinition);
    }

    public IDefinition getDefinition(String definitionId) throws BpmScriptException {
        return versionedDefinitionManager.getDefinition(definitionId);
    }

    public List<IDefinition> getDefinitionsByName(String name) throws BpmScriptException {
        return versionedDefinitionManager.getDefinitionsByName(name);
    }

    public IDefinition getPrimaryDefinition(String name) throws BpmScriptException {
        return versionedDefinitionManager.getPrimaryDefinition(name);
    }

    public List<IDefinition> getPrimaryDefinitions() throws BpmScriptException {
        return versionedDefinitionManager.getPrimaryDefinitions();
    }

    public void setDefinitionAsPrimary(String definitionId) throws BpmScriptException {
        versionedDefinitionManager.setDefinitionAsPrimary(definitionId);
    }

    public void setDefinitionManager(IDefinitionManager definitionManager) {
        versionedDefinitionManager.setDefinitionManager(definitionManager);
    }

    public void setBpmScriptNextChannelName(String bpmScriptNextChannelName) {
        this.bpmScriptNextChannelName = bpmScriptNextChannelName;
    }

    public void setTimeoutChannelName(String timeoutChannelName) {
        this.timeoutChannelName = timeoutChannelName;
    }

    public void setErrorChannelName(String errorChannelName) {
        this.errorChannelName = errorChannelName;
    }

    public void setSpringChannelName(String springChannelName) {
        this.springChannelName = springChannelName;
    }

    public void setBpmScriptFirstChannelName(String bpmScriptFirstChannelName) {
        this.bpmScriptFirstChannelName = bpmScriptFirstChannelName;
    }

    public void setSyncChannelName(String syncChannelName) {
        this.syncChannelName = syncChannelName;
    }

    public Object call(String toCorrelationId, long timeout, Object in) throws Exception {
        return conversationCorrelator.call(toCorrelationId, timeout, in);
    }

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        List<IDefinition> primaryDefinitions = versionedDefinitionManager.getPrimaryDefinitions();
        for (IDefinition primaryDefinition : primaryDefinitions) {
            List<IDefinition> definitionsByName = versionedDefinitionManager.getDefinitionsByName(primaryDefinition.getName());
            for (IDefinition definition : definitionsByName) {
                instanceKiller.killDefinitionInstances(definition.getId(), "Application Shutdown " + new Date());
            }
        }
    }

    /**
     * @see org.bpmscript.integration.internal.IMessageSender#send(java.lang.String, org.bpmscript.integration.internal.IInternalMessage)
     */
    public void send(String address, IInternalMessage internalMessage) {
        bus.send(address, internalMessage);
    }

    public Object call(String definitionName, String methodName, long timeout, Object... args) throws Exception {
        return bpmScriptFacade.call(definitionName, methodName, timeout, args);
    }

    public void callAsync(String definitionName, String methodName, Object... args) throws Exception {
        bpmScriptFacade.callAsync(definitionName, methodName, args);
    }

    public void setVersionedDefinitionManager(MemoryVersionedDefinitionManager versionedDefinitionManager) {
        this.versionedDefinitionManager = versionedDefinitionManager;
    }

    public void setBpmScriptManager(MemoryBpmScriptManager bpmScriptManager) {
        this.bpmScriptManager = bpmScriptManager;
    }

    public void setEngine(BpmScriptEngine engine) {
        this.engine = engine;
    }

    public void setConversationChannelName(String conversationChannelName) {
        this.conversationChannelName = conversationChannelName;
    }

    public void setBus(IMessageBus bus) {
        this.bus = bus;
    }

    public void setConversationCorrelator(ConversationCorrelator conversationCorrelator) {
        this.conversationCorrelator = conversationCorrelator;
    }

    public void setBpmScriptFacade(BpmScriptFacade bpmScriptFacade) {
        this.bpmScriptFacade = bpmScriptFacade;
    }

}
