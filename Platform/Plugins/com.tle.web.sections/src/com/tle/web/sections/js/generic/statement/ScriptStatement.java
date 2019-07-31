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

package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.PreRenderable;
import java.util.Arrays;
import java.util.List;

/** Used for plain old string pieces of Javascript */
public class ScriptStatement implements JSStatements {
  // Snippets
  public static final ScriptStatement WINDOW_CLOSE =
      new ScriptStatement("window.close();"); // $NON-NLS-1$
  public static final ScriptStatement HISTORY_BACK =
      new ScriptStatement("history.back();"); // $NON-NLS-1$

  private String rawExpr;
  private JSExpression expr;
  private List<PreRenderable> preRenderers;

  public ScriptStatement(String rawExpr) {
    this.rawExpr = rawExpr;
    this.expr = null;
  }

  public ScriptStatement(JSExpression expr) {
    this.expr = expr;
    this.rawExpr = null;
  }

  public void setPreRenderers(PreRenderable... preRenderers) {
    this.preRenderers = Arrays.asList(preRenderers);
  }

  public ScriptStatement() {
    // dynamic it
  }

  @Override
  public String getStatements(RenderContext info) {
    return (rawExpr == null ? expr.getExpression(info) + ';' : rawExpr);
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, expr);
    SectionUtils.preRender(info, preRenderers);
  }
}
