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

package org.bpmscript.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.xml.XMLLib.Factory;

/**
 * A context factory that enables the "Dynamic Scope" Context Features for
 * contexts that it creates.
 */
public class DynamicContextFactory extends ContextFactory {
    
    /**
     * Enable the FEATURE_DYNAMIC_SCOPE for contexts otherwise delegates
     * to hasFeatures on ContextFactory.
     * 
     * @see org.mozilla.javascript.ContextFactory#hasFeature(org.mozilla.javascript.Context, int)
     */
    @Override
    protected boolean hasFeature(Context cx, int featureIndex) {
        if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
            return true;
        }
        return super.hasFeature(cx, featureIndex);
    }
    
    /* (non-Javadoc)
     * @see org.mozilla.javascript.ContextFactory#getE4xImplementationFactory()
     */
    @Override
    protected Factory getE4xImplementationFactory() {
        return org.mozilla.javascript.xml.XMLLib.Factory.create(
                "org.bpmscript.mozilla.javascript.xmlimpl.XMLLibImpl"
            );
    }
}
