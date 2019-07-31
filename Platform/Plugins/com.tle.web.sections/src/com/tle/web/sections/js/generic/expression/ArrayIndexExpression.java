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

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ArrayIndexExpression extends AbstractExpression implements JSPropertyExpression {
  private final JSExpression indexExpr;

  public ArrayIndexExpression(Object index) {
    this.indexExpr = JSUtils.convertExpressions(index)[0];
  }

  @Override
  public String getExpression(@Nullable RenderContext info) {
    return '[' + indexExpr.getExpression(info) + ']';
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, indexExpr);
  }

  public static CombinedExpression create(JSExpression base, Object index) {
    return new CombinedExpression(base, new ArrayIndexExpression(index));
  }
}
