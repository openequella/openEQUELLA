package com.tle.web.sections.js.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.expression.BooleanExpression;
import com.tle.web.sections.js.generic.expression.CombinedPropertyExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.expression.SimpleBooleanExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.IfElseStatement;
import com.tle.web.sections.js.generic.statement.IfStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public final class Js
{
	public static final JSCallAndReference ALERT = new ExternallyDefinedFunction("alert", 1);
	public static final String NEWLINE = "\n";

	private Js()
	{
		throw new Error();
	}

	public static JSStatements iff(BooleanExpression condition, JSStatements body)
	{
		return new IfStatement(condition, body);
	}

	public static JSStatements iff(JSExpression condition, JSStatements body)
	{
		return new IfStatement(new SimpleBooleanExpression(condition), body);
	}

	public static JSStatements ife(BooleanExpression condition, JSStatements body, JSStatements alternate)
	{
		return new IfElseStatement(condition, body, alternate);
	}

	public static JSStatements ife(JSExpression condition, JSStatements body, JSStatements alternate)
	{
		return new IfElseStatement(new SimpleBooleanExpression(condition), body, alternate);
	}

	public static JSExpression not(JSExpression condition)
	{
		return new NotExpression(condition);
	}

	public static JSExpression notEquals(JSExpression lhs, JSExpression rhs)
	{
		return new NotEqualsExpression(lhs, rhs);
	}

	public static JSExpression str(String string)
	{
		return new StringExpression(string);
	}

	public static FunctionCallExpression call(JSCallable function, Object... params)
	{
		return new FunctionCallExpression(function, params);
	}

	public static FunctionCallStatement call_s(JSCallable function, Object... params)
	{
		return new FunctionCallStatement(call(function, params));
	}

	public static JSValidator validator(JSCallable function, Object... params)
	{
		return new FunctionCallValidator(function, params);
	}

	public static JSValidator validator(JSExpression expr)
	{
		return new SimpleValidator(expr);
	}

	public static JSHandler handler(JSStatements... statements)
	{
		return new StatementHandler(statements);
	}

	public static JSHandler handler(JSExpression expression)
	{
		return new StatementHandler(new ScriptStatement(expression));
	}

	public static JSHandler handler(JSCallable callable, Object... params)
	{
		return new StatementHandler(call_s(callable, params));
	}

	public static JSStatements alert_s(Object msg)
	{
		return statement(alert(msg));
	}

	public static JSStatements statement(JSExpression ex)
	{
		return new ScriptStatement(ex);
	}

	public static JSStatements statements(JSStatements... st)
	{
		return StatementBlock.get(st);
	}

	public static JSExpression alert(Object msg)
	{
		return new FunctionCallExpression(ALERT, msg);
	}

	public static JSValidator confirm(Label msg)
	{
		return new Confirm(msg);
	}

	public static JSExpression methodCall(JSExpression exp, JSCallable method, Object... params)
	{
		return new CombinedPropertyExpression(exp, new PropertyExpression(new FunctionCallExpression(method, params)));
	}

	public static JSCallAndReference function(String name)
	{
		return new ExternallyDefinedFunction(name);
	}

	public static ScriptVariable var(String name)
	{
		return new ScriptVariable(name);
	}

	/**
	 * @param body
	 * @param params
	 * @return An anonymous function
	 */
	public static JSAssignable function(JSStatements body, JSExpression... params)
	{
		return new AnonymousFunction(body, params);
	}

	public static AssignStatement assign(JSExpression set, Object value)
	{
		return new AssignStatement(set, value);
	}
}
