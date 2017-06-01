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
public abstract class PostfixExpressionParser<RESULT>
{
	public static enum BooleanOp
	{
		NOT_TOKEN("NOT", 3), AND_TOKEN("AND", 2), OR_TOKEN("OR", 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		private static final int GREATEST_PRECEDENCE = Integer.MAX_VALUE;

		private final String toString;
		private final int precedence;

		private BooleanOp(String toString, int precedence)
		{
			this.toString = toString;
			this.precedence = precedence;
		}

		public int getPrecedence()
		{
			return precedence;
		}

		@Override
		public String toString()
		{
			return toString;
		}

		// // STATIC STUFF ////

		public static BooleanOp getOperator(String operator)
		{
			for( BooleanOp op : values() )
			{
				if( op.toString().equalsIgnoreCase(operator) )
				{
					return op;
				}
			}
			return null;
		}
	}

	public PostfixExpressionParser()
	{
		super();
	}

	protected RESULT getResult(String expression)
	{
		Stack<Pair<RESULT, Integer>> operands = new Stack<Pair<RESULT, Integer>>();

		for( String token : expression.split("\\s") ) //$NON-NLS-1$
		{
			BooleanOp op = BooleanOp.getOperator(token);
			if( op != null )
			{
				doOperator(operands, op);
			}
			else
			{
				operands.push(new Pair<RESULT, Integer>(processOperand(token), BooleanOp.GREATEST_PRECEDENCE));
			}
		}

		return operands.pop().getFirst();
	}

	protected abstract RESULT processOperand(String token);

	protected void doOperator(Stack<Pair<RESULT, Integer>> operands, BooleanOp operator)
	{
		switch( operator )
		{
			case NOT_TOKEN:
				doNotOperator(operands);
				break;

			case AND_TOKEN:
				doAndOperator(operands);
				break;

			case OR_TOKEN:
				doOrOperator(operands);
				break;

			default:
				throw new IllegalStateException();
		}
	}

	protected void doNotOperator(Stack<Pair<RESULT, Integer>> operands)
	{
		// Nothing to do here
	}

	protected void doAndOperator(Stack<Pair<RESULT, Integer>> operands)
	{
		// Nothing to do here
	}

	protected void doOrOperator(Stack<Pair<RESULT, Integer>> operands)
	{
		// Nothing to do here
	}
}
