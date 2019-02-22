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

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public class LabelExpression extends AbstractExpression {
  protected final Label label;
  protected final boolean undefinedIfEmpty;

  public LabelExpression(Label label) {
    this(label, false);
  }

  public LabelExpression(Label label, boolean undefinedIfEmpty) {
    this.label = label;
    this.undefinedIfEmpty = undefinedIfEmpty;
  }

  @Override
  public void preRender(PreRenderContext info) {
    // nothing
  }

  @SuppressWarnings("nls")
  @Override
  public String getExpression(@Nullable RenderContext info) {
    String text = label.getText();
    if (undefinedIfEmpty && Check.isEmpty(text)) {
      return "undefined";
    }
    return JSUtils.toJSString(text);
  }
}
