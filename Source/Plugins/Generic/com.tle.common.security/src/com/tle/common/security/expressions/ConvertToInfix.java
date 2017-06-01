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

package com.tle.common.security.expressions;

import java.util.Stack;

import com.tle.common.Pair;

/**
 * @author Nicholas Read
 */
public class ConvertToInfix extends PostfixExpressionParser<String>
{
	public ConvertToInfix()
	{
		super();
	}

	public String convertToInfix(String expression)
	{
		return getResult(expression);
	}

	@Override
	protected String processOperand(String token)
	{
		return token;
	}

	@Override
	protected void doNotOperator(Stack<Pair<String, Integer>> operands)
	{
		Pair<String, Integer> pair = operands.pop();

		String value = possiblyGroupValue(pair, BooleanOp.NOT_TOKEN.getPrecedence());
		value = BooleanOp.NOT_TOKEN.toString() + ' ' + value;

		operands.push(new Pair<String, Integer>(value, BooleanOp.NOT_TOKEN.getPrecedence()));
	}

	@Override
	protected void doAndOperator(Stack<Pair<String, Integer>> operands)
	{
		doBinaryOperator(operands, BooleanOp.AND_TOKEN);
	}

	@Override
	protected void doOrOperator(Stack<Pair<String, Integer>> operands)
	{
		doBinaryOperator(operands, BooleanOp.OR_TOKEN);
	}

	protected void doBinaryOperator(Stack<Pair<String, Integer>> operands, BooleanOp operator)
	{
		Pair<String, Integer> first = operands.pop();
		Pair<String, Integer> second = operands.pop();

		String lhs = possiblyGroupValue(second, operator.getPrecedence());
		String rhs = possiblyGroupValue(first, operator.getPrecedence());

		String value = lhs + ' ' + operator.toString() + ' ' + rhs;

		operands.push(new Pair<String, Integer>(value, operator.getPrecedence()));
	}

	private String possiblyGroupValue(Pair<String, Integer> pair, int newPrecedence)
	{
		String value = pair.getFirst();
		if( newPrecedence > pair.getSecond() )
		{
			value = '(' + value + ')';
		}
		return value;
	}
}
