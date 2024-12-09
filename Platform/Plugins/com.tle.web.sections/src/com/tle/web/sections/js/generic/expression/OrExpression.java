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
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

@NonNullByDefault
public class OrExpression extends BooleanExpression {
  protected final JSExpression first;
  protected final JSExpression second;

  public OrExpression(JSExpression first, JSExpression second) {
    this.first = first;
    this.second = second;
  }

  public OrExpression(String first, JSExpression second) {
    this(new ScriptExpression(first), second);
  }

  public OrExpression(String first, String second) {
    this(new ScriptExpression(first), new ScriptExpression(second));
  }

  @Override
  public String getExpression(RenderContext info) {
    StringBuilder text = new StringBuilder(first.getExpression(info));
    text.append(" || "); // $NON-NLS-1$
    text.append(second.getExpression(info));
    return text.toString();
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(first, second);
  }
}
