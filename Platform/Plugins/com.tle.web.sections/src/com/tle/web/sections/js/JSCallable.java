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

package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;

/**
 * This interface is an abstraction of anything that behaves as a function. A JSCallable does not
 * necessarily have to represent an actual Javascript function, it could for example represent a
 * javascript assign statement.
 *
 * @author jmaginnis
 * @see AssignAsFunction
 */
@NonNullByDefault
public interface JSCallable extends JSFunction {
  String getExpressionForCall(RenderContext info, JSExpression... params);
}
