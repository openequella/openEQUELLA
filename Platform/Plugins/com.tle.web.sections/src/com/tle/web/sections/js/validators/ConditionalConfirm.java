package com.tle.web.sections.js.validators;

import java.io.Serializable;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.Label;

/**
 * Only asks for confirmation if supplied condition is met.
 * 
 * @author Aaron
 */
public class ConditionalConfirm extends Confirm implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final JSExpression condition;

	public ConditionalConfirm(JSExpression condition, Label label)
	{
		super(label);
		this.condition = condition;
	}

	@SuppressWarnings("nls")
	@Override
	public String getValidatorExpressionText(RenderContext info)
	{
		return condition.getExpression(info) + " || " + super.getValidatorExpressionText(info);
	}
}
