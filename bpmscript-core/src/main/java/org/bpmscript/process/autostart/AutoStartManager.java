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

package org.bpmscript.process.autostart;

import java.util.Collection;
import java.util.List;

import org.bpmscript.BpmScriptException;
import org.bpmscript.process.IAutoStartManager;
import org.bpmscript.process.IBpmScriptFacade;
import org.bpmscript.process.IDefinition;
import org.bpmscript.process.IInstanceKiller;
import org.bpmscript.process.IInstanceStatusManager;
import org.bpmscript.process.IVersionedDefinitionManager;
import org.bpmscript.timeout.TimeCalculator;

/**
 * Auto Startup Manager
 */
public class AutoStartManager implements IAutoStartManager {

    private final transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
            .getLog(getClass());
    
    private IVersionedDefinitionManager versionedDefinitionManager = null;
    private IBpmScriptFacade bpmScriptFacade = null;
    private IInstanceKiller instanceKiller = null;
    private IInstanceStatusManager instanceStatusManager = null;
    private String autoStartMethodName = "autoStart";
    private long defaultTimeout = TimeCalculator.DEFAULT_INSTANCE.seconds(5);
    private Object startMessage = "START";
    private String killMessage = "Killed by Auto startup";
    
    /**
     * Starts up a particular definition by name. If autoKill is set, will kill any instances running on an old
     * version of the definition.
     * 
     * @see org.bpmscript.process.IAutoStartupManager#startup(java.lang.String)
     */
    public void startup(String name, boolean autoKill) {
        try {

            IDefinition primaryDefinition = versionedDefinitionManager.getPrimaryDefinition(name);
            if(autoKill) {
                List<IDefinition> definitions = versionedDefinitionManager.getDefinitionsByName(name);
                for (IDefinition definition : definitions) {
                    if(!(definition.getId().equals(primaryDefinition.getId()))) {
                        instanceKiller.killDefinitionInstances(definition.getId(), killMessage);
                    }
                }
                if(primaryDefinition != null) {
                    Collection<String> liveInstances = instanceStatusManager.getLiveInstances(primaryDefinition.getId());
                    if(liveInstances == null || liveInstances.size() == 0) {
                        Object call = bpmScriptFacade.call(primaryDefinition.getName(), autoStartMethodName, defaultTimeout, startMessage);
                        if(call == null) {
                            log.debug("auto startup of " + primaryDefinition.getName() + " timed out");
                        }
                    } else {
                        log.debug("instance(s) of " + name + " already running");
                    }
                } else {
                    log.error("Could not find definition with name " + name);
                }
            } else {
                Object call = bpmScriptFacade.call(primaryDefinition.getName(), autoStartMethodName, defaultTimeout, startMessage);
                if(call == null) {
                    log.debug("auto startup of " + primaryDefinition.getName() + " timed out");
                }
            }
        } catch (BpmScriptException e) {
            log.error(e, e);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public void setVersionedDefinitionManager(IVersionedDefinitionManager versionedDefinitionManager) {
        this.versionedDefinitionManager = versionedDefinitionManager;
    }

    public void setBpmScriptFacade(IBpmScriptFacade bpmScriptFacade) {
        this.bpmScriptFacade = bpmScriptFacade;
    }

    public void setInstanceKiller(IInstanceKiller instanceKiller) {
        this.instanceKiller = instanceKiller;
    }

    public void setAutoStartMethodName(String autoStartMethodName) {
        this.autoStartMethodName = autoStartMethodName;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void setStartMessage(Object startMessage) {
        this.startMessage = startMessage;
    }

    public void setInstanceStatusManager(IInstanceStatusManager instanceStatusManager) {
        this.instanceStatusManager = instanceStatusManager;
    }

    public void setKillMessage(String killMessage) {
        this.killMessage = killMessage;
    }

}
