package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.ReloadFunction;

public class ReloadStatement implements JSStatements
{
	private final JSStatements statement;

	public ReloadStatement()
	{
		this(true);
	}

	public ReloadStatement(boolean validate)
	{
		this.statement = new FunctionCallStatement(new ReloadFunction(validate));
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return statement.getStatements(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(statement);
	}
}
