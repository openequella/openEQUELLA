/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.standard.js.modules;

import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class SelectModule implements JavascriptModule {
  private static final long serialVersionUID = 1L;
  public static final PluginResourceHelper r = ResourcesService.getResourceHelper(JSONModule.class);

  public static final IncludeFile INCLUDE = new IncludeFile(r.url("js/select.js"));
  public static final JSCallable SELECTED_TEXT =
      new ExternallyDefinedFunction("getSelectedText", 1, INCLUDE);
  public static final JSCallable SELECTED_TEXTS =
      new ExternallyDefinedFunction("getSelectedTexts", 1, INCLUDE);
  public static final JSCallable SELECTED_VALUES =
      new ExternallyDefinedFunction("getSelectedValues", 1, INCLUDE);
  public static final JSCallable SELECTED_VALUE =
      new ExternallyDefinedFunction("getSelectedValue", 1, INCLUDE);
  public static final JSCallable ALL_VALUES =
      new ExternallyDefinedFunction("getAllSelectValues", 1, INCLUDE);
  public static final JSCallable SET_VALUE =
      new ExternallyDefinedFunction("setSelectedValue", 2, INCLUDE);
  public static final JSCallable SELECT_ALL =
      new ExternallyDefinedFunction("selectAll", 1, INCLUDE);
  public static final JSCallable ADD_OPTION =
      new ExternallyDefinedFunction("addOption", 3, INCLUDE);
  public static final JSCallable REMOVE_SELECTED =
      new ExternallyDefinedFunction("removeSelected", 1, INCLUDE);
  public static final JSCallable RESET_SELECTED_VALUES =
      new ExternallyDefinedFunction("resetSelectedValues", 1, INCLUDE);

  @Override
  public String getDisplayName() {
    return r.getString("js.modules.select.name");
  }

  @Override
  public String getId() {
    return "select";
  }

  @Override
  public PreRenderable getPreRenderer() {
    return INCLUDE;
  }
}
