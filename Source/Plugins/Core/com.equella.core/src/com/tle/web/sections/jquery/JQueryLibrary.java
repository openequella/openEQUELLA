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

package com.tle.web.sections.jquery;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.javascript.JavascriptLibrary;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.libraries.JQueryBiggerLink;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryDimensions;
import com.tle.web.sections.jquery.libraries.JQueryDraggable;
import com.tle.web.sections.jquery.libraries.JQueryDroppable;
import com.tle.web.sections.jquery.libraries.JQueryFakeSelect;
import com.tle.web.sections.jquery.libraries.JQueryIDTabs;
import com.tle.web.sections.jquery.libraries.JQueryScrollTo;
import com.tle.web.sections.jquery.libraries.JQueryTableSorter;
import com.tle.web.sections.jquery.libraries.JQueryTabs;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.jquery.libraries.JQueryTimer;
import com.tle.web.sections.jquery.libraries.JQueryTreeView;
import com.tle.web.sections.jquery.libraries.JQueryUICore;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

@Bind
@Singleton
public class JQueryLibrary implements JavascriptLibrary {
  private static final long serialVersionUID = 1L;

  private final Map<String, JavascriptModule> moduleMap;

  public JQueryLibrary() {
    List<JavascriptModule> modules = new ArrayList<JavascriptModule>();
    modules.add(new JQueryBiggerLink());
    modules.add(new JQueryCore());
    modules.add(new JQueryDimensions());
    modules.add(new JQueryDraggable());
    modules.add(new JQueryDroppable());
    modules.add(new JQueryFakeSelect());
    modules.add(new JQueryIDTabs());
    modules.add(new JQueryScrollTo());
    modules.add(new JQueryTableSorter());
    modules.add(new JQueryTabs());
    modules.add(new JQueryTextFieldHint());
    modules.add(new JQueryTimer());
    modules.add(new JQueryTreeView());
    modules.add(new JQueryUICore());
    modules.add(new JQueryUIEffects());

    Map<String, JavascriptModule> tempModuleMap = new HashMap<String, JavascriptModule>();
    for (JavascriptModule module : modules) {
      tempModuleMap.put(module.getId(), module);
    }
    moduleMap = Collections.unmodifiableMap(tempModuleMap);
  }

  @Override
  public String getDisplayName() {
    return CurrentLocale.get("com.tle.web.sections.jquery.library.name");
  }

  @Override
  public String getId() {
    return "jquery"; //$NON-NLS-1$
  }

  @Override
  public Map<String, JavascriptModule> getModules() {
    return moduleMap;
  }
}
