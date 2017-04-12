package com.tle.web.sections.result.util;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.PreRenderable;

public class CloseWindowResult implements PreRenderable
{
	private final JSStatements statements;

	public CloseWindowResult(JSStatements... statements)
	{
		this.statements = StatementBlock.get(statements);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addReadyStatements(statements);
	}
}
