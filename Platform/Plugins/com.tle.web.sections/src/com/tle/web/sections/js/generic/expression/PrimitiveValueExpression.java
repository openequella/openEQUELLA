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

import com.tle.web.sections.js.ServerSideValue;

public class PrimitiveValueExpression extends ScriptExpression implements ServerSideValue {
  public PrimitiveValueExpression(int expr) {
    super(Integer.toString(expr));
  }

  public PrimitiveValueExpression(long expr) {
    super(Long.toString(expr));
  }

  public PrimitiveValueExpression(double expr) {
    super(Double.toString(expr));
  }

  public PrimitiveValueExpression(boolean expr) {
    super(Boolean.toString(expr));
  }

  @Override
  public String getJavaString() {
    return expr;
  }
}
