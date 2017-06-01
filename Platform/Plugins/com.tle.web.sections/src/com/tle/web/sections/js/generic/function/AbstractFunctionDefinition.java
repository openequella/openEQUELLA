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

package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.PreRenderable;

/**
 * IMPORTANT: subclasses of this will assume the body won't ever change.
 * <p>
 * If you want to change this behaviour you need to override
 * {@link #getBody(RenderContext)}.
 * 
 * @author jolz
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractFunctionDefinition implements PreRenderable
{
	@Nullable
	protected JSStatements body;
	@Nullable
	protected JSExpression[] params;

	protected String getDefinition(@Nullable RenderContext context)
	{
		StringBuilder sbuf = new StringBuilder();
		sbuf.append("function");
		String name = getFunctionName(context);
		if( !Check.isEmpty(name) )
		{
			sbuf.append(' ');
			sbuf.append(name);
		}
		sbuf.append('(');
		boolean first = true;
		JSExpression[] paramdefs = getParams(context);
		if( paramdefs != null )
		{
			for( JSExpression paramExpr : paramdefs )
			{
				if( !first )
				{
					sbuf.append(',');
				}
				first = false;
				sbuf.append(paramExpr.getExpression(context));
			}
		}
		sbuf.append("){").append(getBody(context).getStatements(context)).append("}").append(Js.NEWLINE);
		return sbuf.toString();
	}

	@Nullable
	protected abstract String getFunctionName(@Nullable RenderContext context);

	@Override
	public void preRender(PreRenderContext context)
	{
		context.preRender(getBody(context));
		context.preRender(getParams(context));
	}

	protected JSStatements getBody(@Nullable RenderContext context)
	{
		return body;
	}

	@Nullable
	protected JSExpression[] getParams(@Nullable RenderContext context)
	{
		return params;
	}
}
