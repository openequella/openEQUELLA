package com.tle.web.sections.js.validators;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;

public class StatementValidator implements JSValidator
{
	private final JSStatements validatorStatements;
	private boolean returnFalse;

	public StatementValidator(JSStatements statements)
	{
		this.validatorStatements = statements;
	}

	public boolean isReturnFalse()
	{
		return returnFalse;
	}

	public StatementValidator setReturnFalse(boolean returnFalse)
	{
		this.returnFalse = returnFalse;
		return this;
	}

	@Override
	public JSValidator setFailureStatements(JSStatements statements)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return validatorStatements.getStatements(info) + (returnFalse ? "return false;" : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, validatorStatements);
	}

}
