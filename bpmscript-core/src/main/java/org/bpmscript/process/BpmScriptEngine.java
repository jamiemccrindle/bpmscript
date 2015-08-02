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
package org.bpmscript.process;

import org.bpmscript.BpmScriptException;
import org.bpmscript.ICompletedResult;
import org.bpmscript.IExecutorResult;
import org.bpmscript.IFailedResult;
import org.bpmscript.IKilledResult;
import org.bpmscript.IPausedResult;
import org.bpmscript.IProcessDefinition;
import org.bpmscript.IProcessExecutor;
import org.bpmscript.ProcessState;
import org.bpmscript.exec.IgnoredResult;
import org.bpmscript.journal.IContinuationJournal;
import org.bpmscript.process.listener.LoggingInstanceListener;

/**
 * A {@link IBpmScriptEngine} implementation. Requires that the following are set on it:
 * 
 * <ul>
 *  <li>instanceManager - an {@link IInstanceManager} service that manages instances (creation and lookup)</li>
 *  <li>versionedDefinitionManager - a {@link IVersionedDefinitionManager} for lookup of definitions</li>
 *  <li>continuationJournal - a journal of instance states - {@link IContinuationJournal}</li>
 *  <li>processExecutorLookup - a process executor lookup (e.g. javascript or java) {@link IProcessExecutorLookup}</li>
 * </ul>
 * 
 */
public class BpmScriptEngine implements IBpmScriptEngine {
    
    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
    .getLog(getClass());

	private IInstanceManager instanceManager;
	private IVersionedDefinitionManager versionedDefinitionManager;
	private IContinuationJournal continuationJournal;
	private IProcessExecutorLookup processExecutorLookup;
	private IInstanceListener instanceListener = new LoggingInstanceListener();

	/**
	 * @see org.bpmscript.process.IBpmScriptEngine#sendFirst(String, String, String, Object)
	 */
	public void sendFirst(final String parentVersion, String definitionName, final String operation, final Object message) throws BpmScriptException {
		final IDefinition definition = versionedDefinitionManager.getPrimaryDefinition(definitionName);
		if(definition == null) {
		    throw new BpmScriptException("Could not find definition named " + definitionName);
		}
		
		final String pid = instanceManager.createInstance(parentVersion, definition.getId(), definition.getName(), definition.getType(), operation);
        final String branch = continuationJournal.createMainBranch(pid);
        final String version = continuationJournal.createVersion(pid, branch);
		instanceListener.instanceCreated(pid, definition);
		try {
			IExecutorResult result = (IExecutorResult) instanceManager.doWithInstance(pid, new IInstanceCallback() {
				public IExecutorResult execute(IInstance processInstance) throws Exception {
					return processExecutorLookup.lookup(definition.getType()).sendFirst(definition.getId(), pid, branch, version, definition, message, operation, parentVersion);
				}
			});
			notifyListener(pid, result);
		} catch (BpmScriptException e) {
			throw e;
		} catch (Throwable e) {
			throw new BpmScriptException(e);
		}
	}

	/**
	 * Notifies the listener based on the type of executor result
	 * @param pid the instance that has changed state
	 * @param result the state change.
	 */
	protected void notifyListener(final String pid, IExecutorResult result) {
		if (result instanceof IPausedResult) {
			instanceListener.instancePaused(pid, (IPausedResult) result);
		} else if (result instanceof IFailedResult) {
			instanceListener.instanceFailed(pid, (IFailedResult) result);
        } else if (result instanceof ICompletedResult) {
            instanceListener.instanceCompleted(pid, (ICompletedResult) result);
        } else if (result instanceof IKilledResult) {
            instanceListener.instanceKilled(pid, (IKilledResult) result);
		}
	}

	/**
	 * @see org.bpmscript.process.IBpmScriptEngine#sendNext(String, String, Object, String)
	 */
	public void sendNext(final String pid, final String branch, final Object message, final String queueId) throws BpmScriptException {
		try {
			IExecutorResult result = (IExecutorResult) instanceManager.doWithInstance(pid, new IInstanceCallback() {
				public IExecutorResult execute(IInstance processInstance) throws Exception {
                    if(processInstance == null) {
                        log.debug("could not find instance with id " + pid + " so returning ignored result");
                        return new IgnoredResult(pid, branch, null);
                    }
                    final ProcessState state = continuationJournal.getProcessStateLatest(branch);
					if(state == ProcessState.PAUSED) {
	                    final String version = continuationJournal.createVersion(pid, branch);
						return processExecutorLookup.lookup(processInstance.getDefinitionType()).sendNext(processInstance.getDefinitionId(), processInstance.getDefinitionName(), pid, branch, version, queueId, message);
					} else {
					    // TODO: we're not storing ignored results for now... it makes sense for things like timeouts
					    log.debug("ignoring message " + message + " for pid and branch " + pid + ", " + branch);
						return new IgnoredResult(pid, branch, null);
					}
				}
			});
			notifyListener(pid, result);
		} catch (BpmScriptException e) {
			throw e;
		} catch (Throwable e) {
			throw new BpmScriptException(e);
		}
	}

    /**
     * @see org.bpmscript.process.IBpmScriptEngine#kill(String, String, String)
     */
    public void kill(final String pid, final String branch, final String message) throws BpmScriptException {
        try {
            IExecutorResult result = (IExecutorResult) instanceManager.doWithInstance(pid, new IInstanceCallback() {
                public IExecutorResult execute(IInstance processInstance) throws Exception {
                    if(processInstance == null) {
                        return new IgnoredResult(pid, branch, null);
                    }
                    final ProcessState state = continuationJournal.getProcessStateLatest(branch);
                    if(state == ProcessState.PAUSED) {
                        final String version = continuationJournal.createVersion(pid, branch);
                        return processExecutorLookup.lookup(processInstance.getDefinitionType()).kill(processInstance.getDefinitionId(), processInstance.getDefinitionName(), pid, branch, version, message);
                    } else {
                        // TODO: we're not storing ignored results for now... it makes sense for things like timeouts
                        log.debug("ignoring message " + message + " for pid and branch " + pid + ", " + branch);
                        return new IgnoredResult(pid, branch, null);
                    }
                }
            });
            notifyListener(pid, result);
        } catch (BpmScriptException e) {
            throw e;
        } catch (Throwable e) {
            throw new BpmScriptException(e);
        }
    }

    /**
     * @param instanceListener a listener with callbacks for each of the states
     *   an instance can go through
     */
	public void setInstanceListener(IInstanceListener instanceListener) {
		this.instanceListener = instanceListener;
	}

	/**
	 * @param instanceManager an instance manager. this must be set
	 */
	public void setInstanceManager(IInstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	/**
	 * @param versionedDefinitionManager a versioned definition manager. this must be set.
	 */
	public void setVersionedDefinitionManager(
			IVersionedDefinitionManager versionedDefinitionManager) {
		this.versionedDefinitionManager = versionedDefinitionManager;
	}

	/**
	 * @param continuationJournal a continuation journal. this must be set.
	 */
	public void setContinuationJournal(IContinuationJournal continuationJournal) {
		this.continuationJournal = continuationJournal;
	}

	public IInstanceListener getInstanceListener() {
		return instanceListener;
	}

	public IInstanceManager getInstanceManager() {
		return instanceManager;
	}

	public IContinuationJournal getContinuationJournal() {
		return continuationJournal;
	}

	public IVersionedDefinitionManager getVersionedDefinitionManager() {
		return versionedDefinitionManager;
	}

    /**
     * @see org.bpmscript.process.IBpmScriptEngine#validate(IProcessDefinition)
     */
    public String validate(IProcessDefinition processDefinition) throws BpmScriptException {
        String type = processDefinition.getType();
        if(type == null) {
            throw new BpmScriptException("type is null for definition " + processDefinition);
        }
        IProcessExecutor processExecutor = processExecutorLookup.lookup(type);
        if(processExecutor == null) {
            throw new BpmScriptException("processExecutor is null for type " + processDefinition);
        }
        return processExecutor.validate(processDefinition);
    }

    /**
     * @param processExecutorLookup the factory that is used to look up the right {@link IProcessExecutor}
     *   based on what type the definition is (e.g. java or javascript)
     */
    public void setProcessExecutorLookup(IProcessExecutorLookup processExecutorLookup) {
        this.processExecutorLookup = processExecutorLookup;
    }

}
