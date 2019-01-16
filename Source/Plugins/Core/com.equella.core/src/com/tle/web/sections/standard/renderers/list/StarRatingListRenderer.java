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

package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryStarRating;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.standard.model.HtmlListState;

@SuppressWarnings("nls")
public class StarRatingListRenderer extends DropDownRenderer {
  public StarRatingListRenderer(HtmlListState listState) {
    super(listState);
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    // The JQuery library requires a DIV wrapper
    writer.writeTag("div");
    super.writeStart(writer, attrs);
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    super.writeEnd(writer);
    writer.endTag("div");
  }

  @Override
  public void preRender(PreRenderContext info) {
    super.preRender(info);

    final ObjectExpression oe = new ObjectExpression();
    oe.put("inputType", "select");

    JQueryStarRating.starRating(
        info,
        PropertyExpression.create(new JQuerySelector(this), new ScriptExpression("parent()")),
        oe);
  }
}
