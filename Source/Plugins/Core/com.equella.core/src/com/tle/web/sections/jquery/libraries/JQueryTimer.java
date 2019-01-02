/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.DebugSettings;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.PreRenderable;

public class JQueryTimer implements JavascriptModule
{
	private static final long serialVersionUID = 1L;
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.timer.js").hasMin();

	@SuppressWarnings("nls")
	private static final ExternallyDefinedFunction TIMER_FUNC = new ExternallyDefinedFunction(
		PropertyExpression.create(JQueryCore.JQUERY, "timer"), -1, PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.timer.name");
	}

	public static JSStatements createTimer(int millis, JSStatements body, ScriptVariable... vars)
	{
		return new FunctionCallStatement(TIMER_FUNC, millis, new AnonymousFunction(body, vars));
	}

	@Override
	public String getId()
	{
		return "timer";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
