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

package com.tle.web.sections.ajax.handler;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.*;
import com.tle.web.sections.ajax.AjaxEffects;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.events.js.EventModifier;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.ServerSideValue;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@NonNullByDefault
public class AjaxGeneratorImpl implements AjaxGenerator {
  private final String baseId;
  private final Map<String, ParameterizedEvent> methods = new HashMap<String, ParameterizedEvent>();

  public AjaxGeneratorImpl(String baseId) {
    this.baseId = baseId;
  }

  @Nullable
  @Override
  public JSCallable getEffectFunction(EffectType type, String urlSpinner) {
    JSCallable stdFunc = null;
    switch (type) {
      case REPLACE_WITH_LOADING:
        stdFunc = AjaxEffects.FUNCTION_UPDATE_WITH_LOADING;
        break;
      case REPLACE_IN_PLACE:
        stdFunc = AjaxEffects.FUNCTION_UPDATE_DOM_SILENT;
        break;
      case FADEIN:
        stdFunc = AjaxEffects.FUNCTION_UPDATE_DOM_FADEIN;
        break;
      case ACTIVITY:
        stdFunc = AjaxEffects.FUNCTION_UPDATE_WITH_ACTIVITY;
        break;
      case FADEOUTIN:
        stdFunc = AjaxEffects.FUNCTION_FADE_DOM;
        break;
      case FADEOUTIN_ONRESULTS:
        return AjaxEffects.FUNCTION_FADE_DOM_RESULTS;
      default:
        return null;
    }
    return new PrependedParameterFunction(stdFunc, urlSpinner);
  }

  @Nullable
  @Override
  public JSCallable getEffectFunction(EffectType type) {
    return getEffectFunction(type, URL_SPINNER);
  }

  @Override
  public JSBookmarkModifier getUpdateDomModifier(
      SectionTree tree,
      @Nullable SectionId modalId,
      String ajaxId,
      ParameterizedEvent event,
      Object... params) {
    final UpdateDomEvent domEvent = UpdateDomEvent.register(tree, modalId, event, ajaxId);
    return new EventModifier(domEvent.getEventId(), event, params);
  }

  @Override
  public UpdateDomFunction getAjaxUpdateDomFunctionWithCallback(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      JSCallable onSuccess,
      String... ajaxIds) {
    UpdateDomEvent domEvent = UpdateDomEvent.register(tree, modalId, event, ajaxIds);
    return new UpdateDomFunction(domEvent, ajaxIds[0], effectFunction, onSuccess);
  }

  @Override
  public UpdateDomFunction getAjaxUpdateDomFunction(
      SectionTree tree, @Nullable SectionId modalId, ParameterizedEvent event, String... ajaxIds) {
    return getAjaxUpdateDomFunctionWithCallback(
        tree, modalId, event, getEffectFunction(EffectType.REPLACE_WITH_LOADING), null, ajaxIds);
  }

  @Override
  public UpdateDomFunction getAjaxUpdateDomFunction(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      String... ajaxIds) {
    return getAjaxUpdateDomFunctionWithCallback(
        tree, modalId, event, effectFunction, null, ajaxIds);
  }

  @Override
  public UpdateDomFunction getAjaxUpdateDomFunction(
      String customEQ,
      SectionTree tree,
      SectionId modalId,
      ParameterizedEvent event,
      JSCallable effectFunction,
      String... ajaxIds) {
    UpdateDomEvent domEvent = UpdateDomEvent.register(tree, modalId, event, ajaxIds);
    return new UpdateDomFunction(domEvent, ajaxIds[0], effectFunction, null, customEQ);
  }

  @Override
  public UpdateDomFunction getAjaxUpdateDomFunctionWithCallback(
      SectionTree tree,
      @Nullable SectionId modalId,
      ParameterizedEvent event,
      JSCallable onSuccess,
      String... ajaxIds) {
    return getAjaxUpdateDomFunctionWithCallback(
        tree,
        modalId,
        event,
        getEffectFunction(EffectType.REPLACE_WITH_LOADING),
        onSuccess,
        ajaxIds);
  }

  public static BookmarkModifier getModifier(String baseId, final String name, Object... params) {
    final JSExpression[] exprs = JSUtils.convertExpressions(params);
    return new BookmarkModifier() {
      @Override
      public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState) {
        bookmarkState.put(EventGeneratorListener.EVENT_ID, new String[] {baseId + '.' + name});
        for (int i = 0; i < exprs.length; i++) {
          bookmarkState.put(
              EventGeneratorListener.EVENT_PARAM + i,
              new String[] {((ServerSideValue) exprs[i]).getJavaString()});
        }
      }
    };
  }

  @Override
  public Bookmark getModifiedAjaxUrl(
      SectionInfo info, BookmarkModifier modifier, String name, Object... params) {
    InfoBookmark bm = new InfoBookmark(info, info.<URI>getAttribute(AjaxGenerator.AJAX_BASEURI));
    return new BookmarkAndModify(bm, getModifier(baseId, name, params), modifier);
  }

  @Override
  public Bookmark getAjaxUrl(SectionInfo info, String name, Object... params) {
    InfoBookmark bm = new InfoBookmark(info, info.<URI>getAttribute(AjaxGenerator.AJAX_BASEURI));
    return new BookmarkAndModify(bm, getModifier(baseId, name, params));
  }

  @Override
  public JSCallable getAjaxFunction(String name) {
    return new AjaxFunction(baseId + '.' + name, methods.get(name).getParameterCount());
  }

  public void registerWithListener(EventGeneratorListener listener) {
    for (ParameterizedEvent pevent : methods.values()) {
      listener.registerHandler(pevent);
    }
  }

  public void addEventCreator(String methodName, AjaxEventCreator creator) {
    methods.put(methodName, creator);
  }
}
