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

package com.tle.web.sections.jquery;

import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

public final class Jq {
  private Jq() {
    throw new Error();
  }

  public static JSExpression methodCall(ElementId obj, JSCallable method, Object... params) {
    return JQuerySelector.methodCallExpression(obj, method, params);
  }

  public static JQuerySelector selector(String id) {
    return selector(Type.RAW, id);
  }

  public static JQuerySelector $(String id) {
    return selector(Type.RAW, id);
  }

  public static JQuerySelector $(JSExpression exp) {
    return new JQuerySelector(exp);
  }

  public static JQuerySelector $(ElementId element) {
    element.registerUse();
    return new JQuerySelector(element);
  }

  public static JQuerySelector selector(Type type, String idOrClass) {
    return new JQuerySelector(type, idOrClass);
  }

  public static JQuerySelector $(Type type, String idOrClass) {
    return new JQuerySelector(type, idOrClass);
  }

  public static JSExpression $val(ElementId element) {
    return JQuerySelector.valueGetExpression(element);
  }

  public static JSExpression $val(ElementId element, JSExpression value) {
    return JQuerySelector.valueSetExpression(element, value);
  }
}
