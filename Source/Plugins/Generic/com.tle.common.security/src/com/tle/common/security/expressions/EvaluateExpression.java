package com.tle.common.security.expressions;

import java.util.Stack;

import com.tle.common.Pair;

/**
 * @author Nicholas Read
 */
public class EvaluateExpression extends PostfixExpressionParser<Boolean>
{
	public EvaluateExpression()
	{
		super();
	}

	protected boolean evaluate(String expression)
	{
		return getResult(expression);
	}

	@Override
	protected Boolean processOperand(String token)
	{
		return Boolean.valueOf(token);
	}

	@Override
	protected void doNotOperator(Stack<Pair<Boolean, Integer>> operands)
	{
		boolean value = operands.pop().getFirst();
		value = !value;
		operands.push(new Pair<Boolean, Integer>(value, BooleanOp.NOT_TOKEN.getPrecedence()));
	}

	@Override
	protected void doAndOperator(Stack<Pair<Boolean, Integer>> operands)
	{
		boolean operand1 = operands.pop().getFirst();
		boolean operand2 = operands.pop().getFirst();
		operands.push(new Pair<Boolean, Integer>(operand1 && operand2, BooleanOp.AND_TOKEN.getPrecedence()));
	}

	@Override
	protected void doOrOperator(Stack<Pair<Boolean, Integer>> operands)
	{
		boolean operand1 = operands.pop().getFirst();
		boolean operand2 = operands.pop().getFirst();
		operands.push(new Pair<Boolean, Integer>(operand1 || operand2, BooleanOp.OR_TOKEN.getPrecedence()));
	}
}
