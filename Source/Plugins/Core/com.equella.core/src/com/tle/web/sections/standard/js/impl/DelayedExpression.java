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

package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.RuntimeExpression;
import com.tle.web.sections.js.generic.function.DynamicNamedFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.standard.js.DelayedRenderer;

public abstract class DelayedExpression<T> extends RuntimeExpression
{
	private final DelayedRenderer<T> delayedRenderer;
	private final ElementId id;
	private final String prefix;

	public DelayedExpression(DelayedRenderer<T> delayedRenderer, String prefix, ElementId id)
	{
		this.delayedRenderer = delayedRenderer;
		this.prefix = prefix;
		this.id = id;
	}

	@Override
	protected JSExpression createExpression(RenderContext info)
	{
		T renderer = delayedRenderer.getSelectedRenderer(info);
		if( renderer != null )
		{
			return createRealExpression(info, renderer);
		}
		return new FunctionCallExpression(new DynamicNamedFunction(prefix, id)
		{
			@SuppressWarnings("nls")
			@Override
			public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
			{
				T renderer = delayedRenderer.getSelectedRenderer(context);
				if( renderer == null )
				{
					throw new SectionsRuntimeException("Trying to use function '" + prefix + id.getElementId(context)
						+ "' but failed to render component with id:" + id.getElementId(context));
				}
				return new ReturnStatement(createRealExpression(context, renderer));
			}
		});
	}

	protected abstract JSExpression createRealExpression(SectionInfo info, T renderer);
}
