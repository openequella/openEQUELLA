package com.tle.core.security.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.security.expressions.PostfixExpressionParser;

/**
 * @author Nicholas Read
 */
public class AclExpressionCollector extends PostfixExpressionParser<Object>
{
	private List<String> results = new ArrayList<String>();

	public AclExpressionCollector()
	{
		super();
	}

	public List<String> getComponents(String expression)
	{
		if( Check.isEmpty(expression) )
		{
			return Collections.emptyList();
		}
		else
		{
			getResult(expression);
			return results;
		}
	}

	@Override
	protected void doOperator(Stack<Pair<Object, Integer>> operands, BooleanOp operator)
	{
		results.add(operator.toString());
	}

	@Override
	protected Object processOperand(String token)
	{
		results.add(token);
		return null;
	}
}
