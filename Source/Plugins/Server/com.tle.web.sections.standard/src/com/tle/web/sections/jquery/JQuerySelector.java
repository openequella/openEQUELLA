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

package com.tle.web.sections.jquery;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.AbstractExpression;
import com.tle.web.sections.js.generic.expression.CombinedPropertyExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;

public class JQuerySelector extends AbstractExpression
{
	private ElementId elementId;
	private JSExpression expr;
	private JSExpression contextExpr;

	public enum Type
	{
		ID, CLASS, RAW, NAME
	}

	private String getSelectString(Type type, String id)
	{
		String typeChar = "";
		switch( type )
		{
			case ID:
				typeChar = "#";
				break;
			case CLASS:
				typeChar = ".";
				break;
			case RAW:
				typeChar = "";
				break;
			case NAME:
				return "[name=\"" + JSUtils.escape(id, false) + "\"]";
		}
		return typeChar + JSUtils.escape(id, false);
	}

	public JQuerySelector(Object expr)
	{
		this.expr = JSUtils.convertExpression(expr);
	}

	public JQuerySelector(ElementId id)
	{
		this.elementId = id;
		id.registerUse();
	}

	public JQuerySelector(Type type, String id)
	{
		this.expr = new StringExpression(getSelectString(type, id));
	}

	public void setContextExpr(JSExpression contextExpr)
	{
		this.contextExpr = contextExpr;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		if( expr == null )
		{
			expr = new StringExpression(getSelectString(Type.ID, elementId.getElementId(info)));
		}

		return JQueryCore.JQUERY.getExpressionForCall(info, contextExpr == null ? new JSExpression[]{expr}
			: new JSExpression[]{expr, contextExpr});
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, JQueryCore.JQUERY, expr, contextExpr);
	}

	public static JSExpression valueSetExpression(ElementId element, Object value)
	{
		return new CombinedPropertyExpression(new JQuerySelector(element), new PropertyExpression(
			new FunctionCallExpression("val", value)));
	}

	public static JSExpression valueGetExpression(ElementId element)
	{
		return new CombinedPropertyExpression(new JQuerySelector(element), new PropertyExpression(
			new FunctionCallExpression("val")));
	}

	public static JSExpression methodCallExpression(ElementId element, JSCallable method, Object... params)
	{
		return methodCallExpression(new JQuerySelector(element), method, params);
	}

	public static JSExpression methodCallExpression(Type type, String id, JSCallable method, Object... params)
	{
		return methodCallExpression(new JQuerySelector(type, id), method, params);
	}

	private static JSExpression methodCallExpression(JQuerySelector selector, JSCallable method, Object... params)
	{
		return new CombinedPropertyExpression(selector, new PropertyExpression(Js.call(method, params)));
	}
}
