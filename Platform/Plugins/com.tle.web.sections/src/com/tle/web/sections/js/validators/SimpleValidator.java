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

package com.tle.web.sections.js.validators;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.statement.ScriptStatement;

public class SimpleValidator implements JSValidator
{
	private JSStatements failureStatments;
	private final JSExpression validatorExpression;
	private boolean returnFalse = true;

	public SimpleValidator(JSExpression validatorExpression)
	{
		failureStatments = new ScriptStatement(""); //$NON-NLS-1$
		this.validatorExpression = validatorExpression;
	}

	@Override
	public JSValidator setFailureStatements(JSStatements statements)
	{
		this.failureStatments = statements;
		return this;
	}

	public String getValidatorExpressionText(RenderContext info)
	{
		return validatorExpression.getExpression(info);
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		return "if (!(" + getValidatorExpressionText(info) + ")){" + failureStatments.getStatements(info)
			+ (returnFalse ? "return false;" : "") + "}";
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(failureStatments, validatorExpression);
	}

	public void setReturnFalse(boolean returnFalse)
	{
		this.returnFalse = returnFalse;
	}

}
