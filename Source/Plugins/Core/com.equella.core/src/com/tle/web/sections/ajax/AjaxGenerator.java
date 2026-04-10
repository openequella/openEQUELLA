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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.*;
import com.tle.web.sections.ajax.handler.AjaxFunction;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@NonNullByDefault
@SuppressWarnings("nls")
public interface AjaxGenerator {
  PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(AjaxFunction.class);

  String AJAX_BASEURI = "AJAX_BASEURI";
  String AJAXID_BODY = "<BODY>";

  String URL_SPINNER = urlHelper.url("images/ajax/doing.gif");
  String URL_SPINNER_INLINE = urlHelper.url("images/ajax/ajax-loader.gif");
  String URL_SPINNER_LOADING = urlHelper.url("images/ajax/loading.gif");
  ScriptVariable RESULTS_VAR = new ScriptVariable("results");
  ScriptVariable PROPERTY_VALUE = new ScriptVariable("value");
  ScriptVariable STATUS_VAR = new ScriptVariable("status");
  IncludeFile AJAX_LIBRARY =
      new IncludeFile(urlHelper.url("js/ajaxhelper.js"), JQueryCore.PRERENDER);

  // JSCallAndReference UPDATE_INCLUDES = new ExternallyDefinedFunction(
  // "updateIncludes", 2, AJAX_LIBRARY);

  JSCallAndReference OPEN_AJAX_DIALOG =
      new ExternallyDefinedFunction("openAjaxDialog", 7, AJAX_LIBRARY);

  JSCallAndReference OPEN_AJAX_DIALOGURL =
      new ExternallyDefinedFunction("openAjaxDialogUrl", 7, AJAX_LIBRARY);

  JSCallAndReference CLOSE_AJAX_DIALOG =
      new ExternallyDefinedFunction("closeAjaxDialog", 2, AJAX_LIBRARY);

  JSCallAndReference SUBMIT_BODY = new ExternallyDefinedFunction("submitBody", -1, AJAX_LIBRARY);

  enum EffectType {
    REPLACE_WITH_LOADING,
    REPLACE_IN_PLACE,
    FADEIN,
    FADEOUTIN,
    FADEOUTIN_ONRESULTS,
    ACTIVITY
  }

  Bookmark getModifiedAjaxUrl(
      SectionInfo info, BookmarkModifier modifier, String name, Object... params);

  Bookmark getAjaxUrl(SectionInfo info, String name, Object... params);

  /**
   * Create a function which can return a JSON object from the server.
   *
   * <p>The JSCallable of the form: function(callback, args...) {}
   *
   * @param name
   * @return
   */
  JSCallable getAjaxFunction(String name);

  @Nullable
  JSCallable getEffectFunction(EffectType type);

  @Nullable
  JSCallable getEffectFunction(EffectType type, String urlSpinner);

  /**
   * @param tree
   * @param modalId
   * @param event
   * @param ajaxIds
   * @return
   */
  UpdateDomFunction getAjaxUpdateDomFunction(
      SectionTree tree, @Nullable SectionId modalId, ParameterizedEvent event, String... ajaxIds);

  UpdateDomFunction getAjaxUpdateDomFunctionWithCallback(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable onSuccess,
      String... ajaxIds);

  /**
   * Build an Ajax function that updates DOM for the target web element as the handler of a Legacy
   * Section event
   *
   * @param tree The Section tree where this function is registered.
   * @param modalId ID of the Section that creates this function.
   * @param event Event which will be handled by this function.
   * @param effectFunction Additional side effect function to be executed when the DOM is updated.
   * @param ajaxIds ID of the target web element
   */
  UpdateDomFunction getAjaxUpdateDomFunction(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      String... ajaxIds);

  /**
   * Similar to {@link #getAjaxUpdateDomFunction(SectionTree, SectionId, ParameterizedEvent,
   * JSCallable, String...)}, but with an additional parameter customEQ, which is the name of a
   * custom EQ object.
   */
  UpdateDomFunction getAjaxUpdateDomFunction(
      String customEQ,
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      String... ajaxIds);

  UpdateDomFunction getAjaxUpdateDomFunctionWithCallback(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      JSCallable onSuccess,
      String... ajaxIds);

  JSBookmarkModifier getUpdateDomModifier(
      SectionTree tree,
      @Nullable SectionId modalId,
      String ajaxId,
      ParameterizedEvent event,
      Object... params);
}
