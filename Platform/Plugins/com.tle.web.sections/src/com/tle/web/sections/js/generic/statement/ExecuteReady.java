package com.tle.web.sections.js.generic.statement;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.SectionRenderable;

public class ExecuteReady implements SectionRenderable
{
	private JSStatements statements;

	public ExecuteReady(JSCallable callable, Object... args)
	{
		statements = new FunctionCallStatement(callable, args);
	}

	public ExecuteReady(JSStatements statements)
	{
		this.statements = statements;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addReadyStatements(statements);
	}
}
