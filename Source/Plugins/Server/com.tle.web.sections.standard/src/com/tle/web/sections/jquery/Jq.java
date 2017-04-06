package com.tle.web.sections.jquery;

import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

/**
 * @author Aaron
 */
public final class Jq
{
	private Jq()
	{
		throw new Error();
	}

	public static JSExpression methodCall(ElementId obj, JSCallable method, Object... params)
	{
		return JQuerySelector.methodCallExpression(obj, method, params);
	}

	public static JQuerySelector selector(String id)
	{
		return selector(Type.RAW, id);
	}

	public static JQuerySelector $(String id)
	{
		return selector(Type.RAW, id);
	}

	public static JQuerySelector $(JSExpression exp)
	{
		return new JQuerySelector(exp);
	}

	public static JQuerySelector $(ElementId element)
	{
		element.registerUse();
		return new JQuerySelector(element);
	}

	public static JQuerySelector selector(Type type, String idOrClass)
	{
		return new JQuerySelector(type, idOrClass);
	}

	public static JQuerySelector $(Type type, String idOrClass)
	{
		return new JQuerySelector(type, idOrClass);
	}

	public static JSExpression $val(ElementId element)
	{
		return JQuerySelector.valueGetExpression(element);
	}

	public static JSExpression $val(ElementId element, JSExpression value)
	{
		return JQuerySelector.valueSetExpression(element, value);
	}
}
