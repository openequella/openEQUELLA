/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.discoverability.scripting;

import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.web.discoverability.scripting.impl.MetaScriptWrapper;
import com.tle.web.discoverability.scripting.objects.MetaScriptObject;
import com.tle.web.sections.events.PreRenderContext;
import java.util.Map;
import javax.inject.Singleton;

@Bind
@Singleton
public class DiscoverabilityScriptObjectContributor implements ScriptObjectContributor {
  @Override
  public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params) {

    final PreRenderContext render =
        (PreRenderContext) params.getAttributes().get("context"); // $NON-NLS-1$
    if (render != null) {
      objects.put(MetaScriptObject.DEFAULT_VARIABLE, new MetaScriptWrapper(render));
    }
  }
}
