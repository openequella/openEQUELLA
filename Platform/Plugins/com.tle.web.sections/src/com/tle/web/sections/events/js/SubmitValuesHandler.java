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

package com.tle.web.sections.events.js;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

public class SubmitValuesHandler extends OverrideHandler
{
	private final JSExpression[] parameters;
	private final SubmitValuesFunction valuesFunction;

	public SubmitValuesHandler(ParameterizedEvent pevent, Object... params)
	{
		this(new SubmitValuesFunction(pevent), JSUtils.convertExpressions(params));
	}

	public SubmitValuesHandler(SubmitValuesFunction valuesFunction, JSExpression[] params)
	{
		this.parameters = params;
		this.valuesFunction = valuesFunction;
		addStatements(new FunctionCallStatement(valuesFunction, (Object[]) params));
	}

	public JSExpression[] getParameters()
	{
		return parameters;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(parameters);
	}

	public SubmitValuesHandler setValidate(boolean validate)
	{
		valuesFunction.setValidate(validate);
		return this;
	}

	@Override
	public JSBookmarkModifier getModifier()
	{
		return new EventModifier(valuesFunction.getFirstParam(), valuesFunction.getEvent(), (Object[]) parameters);
	}
}
