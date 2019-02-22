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

package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;

/**
 * A JSAssignable represents a function that can be assigned to something. Which is why it also
 * extends JSExpression.
 *
 * <p>For a class to implement this interface, it MUST allow the following snippet of javascript to
 * be correct, where ${expr} is the results of getExpression() and the number of parameters is 2.
 *
 * <pre>
 * var ref = ${expr};
 * ref(1,2);
 * </pre>
 *
 * @author jolz
 */
@NonNullByDefault
public interface JSAssignable extends JSFunction, JSExpression {
  // Nothing to do here
}
