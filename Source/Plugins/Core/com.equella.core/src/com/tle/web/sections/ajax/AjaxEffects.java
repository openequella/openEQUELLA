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

package com.tle.web.sections.ajax;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.ajax.handler.AjaxFunction;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class AjaxEffects {
  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(AjaxFunction.class);

  public static final IncludeFile EFFECTS_LIB =
      new IncludeFile(resources.url("js/ajaxeffects.js"), AjaxGenerator.AJAX_LIBRARY);
  private static final JSCallAndReference EFFECTS_CLASS =
      new ExternallyDefinedFunction("AjaxFX", EFFECTS_LIB);

  public static final ExternallyDefinedFunction FUNCTION_UPDATE_WITH_LOADING =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "updateDomWithLoading", -1, EFFECTS_LIB);
  public static final ExternallyDefinedFunction FUNCTION_UPDATE_DOM_SILENT =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "updateDomSilent", -1, EFFECTS_LIB);
  public static final ExternallyDefinedFunction FUNCTION_UPDATE_DOM_FADEIN =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "updateDomFadeIn", -1, EFFECTS_LIB);
  public static final ExternallyDefinedFunction FUNCTION_FADE_DOM =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "fadeDom", -1, EFFECTS_LIB);
  public static final ExternallyDefinedFunction FUNCTION_FADE_DOM_RESULTS =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "fadeDomResults", -1, EFFECTS_LIB);
  public static final ExternallyDefinedFunction FUNCTION_UPDATE_WITH_ACTIVITY =
      new ExternallyDefinedFunction(EFFECTS_CLASS, "updateDomWithActivity", -1, EFFECTS_LIB);

  private AjaxEffects() {
    throw new Error();
  }
}
