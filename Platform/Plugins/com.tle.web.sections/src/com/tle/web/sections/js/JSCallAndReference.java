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
 * Interface that represents a callable and assignable javascript function.
 * <p>
 * For a class to implement this interface, it MUST allow the following snippet
 * of javascript to be correct, where ${expr} is the results of getExpression(),
 * ${call} is the result of getCallForExpression() and the number of parameters
 * is 2.
 * 
 * <pre>
 * var ref = ${expr};
 * ref(1,2);
 * ${call(1, 2)};
 * </pre>
 * <p>
 * JSCallAndReference could be considered as representing a standard javascript
 * function.
 * 
 * @author jolz
 */
@NonNullByDefault
public interface JSCallAndReference extends JSCallable, JSAssignable
{

	/**
	 * If it is static, the result of
	 * {@link #getExpression(com.tle.web.sections.events.RenderContext)} ,
	 * {@link #getExpressionForCall(com.tle.web.sections.events.RenderContext, JSExpression...)}
	 * and {@link #getNumberOfParams(com.tle.web.sections.events.RenderContext)}
	 * will never change. It means that
	 * {@link #getExpression(com.tle.web.sections.events.RenderContext)} can be
	 * called with a {@code null} {@code RenderContext}.
	 * 
	 * @return Whether or not this is static
	 */
	boolean isStatic();
}
