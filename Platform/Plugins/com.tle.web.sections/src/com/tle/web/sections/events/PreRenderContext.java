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

package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;
import java.util.Collection;
import java.util.Map;

@NonNullByDefault
public interface PreRenderContext extends RenderContext {
  @Override
  void preRender(@Nullable Collection<? extends PreRenderable> preRenderers);

  @Override
  void preRender(PreRenderable preRenderer);

  @Override
  void preRender(PreRenderable... preRenderers);

  void addJs(String src);

  void addCss(String src);

  void addCss(CssInclude css);

  void addStatements(JSStatements statements);

  void addFooterStatements(JSStatements statements);

  void addReadyStatements(JSStatements statements);

  void addHeaderMarkup(String head);

  void bindHandler(String event, Map<String, String> attrs, JSHandler handler);
}
