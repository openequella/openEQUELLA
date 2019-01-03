/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.jquery.libraries;

import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public final class JQueryProgression
{
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.progression.js");

	private static final ExternallyDefinedFunction FUNC = new ExternallyDefinedFunction("progression", PRERENDER);

	public static JSStatements createProgression(JQuerySelector selector)
	{
		return new JQueryStatement(selector, new FunctionCallExpression(FUNC));
	}

	public static JSStatements createProgression()
	{
		return createProgression(new JQuerySelector(Type.CLASS, "progressbar"));
	}

	private JQueryProgression()
	{
		throw new Error();
	}
}