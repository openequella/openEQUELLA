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

package com.tle.web.sections.equella.render;

import com.tle.common.Check;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryJqtransform;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;
import java.util.Set;

@SuppressWarnings("nls")
public class JqtransformDropDownRenderer extends DropDownRenderer {
  private static final String KEY_OPENER_CLASS = "selectOpenerClass";
  private static final String KEY_WRAPPER_CLASS = "wrapperClass";

  public JqtransformDropDownRenderer(HtmlListState state) {
    super(state);
  }

  @Override
  public void preRender(PreRenderContext info) {
    final ObjectExpression params = new ObjectExpression();
    final HtmlComponentState selectState = getHtmlState();

    String openerClass = selectState.getAttribute(KEY_OPENER_CLASS);
    if (openerClass != null) {
      params.put(KEY_OPENER_CLASS, openerClass);
    }

    boolean hasClass = false;

    // inherit any classes put on the SELECT element
    final Set<String> classes = selectState.getStyleClasses();
    final StringBuilder classString = new StringBuilder();
    if (!Check.isEmpty(classes)) {
      for (String clas : classes) {
        if (hasClass) {
          classString.append(' ');
        }
        classString.append(clas);
        hasClass = true;
      }
    }
    final String wrapperClass = selectState.getAttribute(KEY_WRAPPER_CLASS);
    if (!Check.isEmpty(wrapperClass)) {
      if (hasClass) {
        classString.append(' ');
      }
      classString.append(wrapperClass);
      hasClass = true;
    }
    if (hasClass) {
      params.put(KEY_WRAPPER_CLASS, classString.toString());
    }

    info.addReadyStatements(JQueryJqtransform.setupJqtransform(Jq.$(this), params));
    super.preRender(info);
  }
}
