package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;

/**
 * A JSStatements where the contents can be decided at runtime.
 * 
 * @author jolz
 */
@NonNullByDefault
public class RuntimeStatement implements JSStatements
{
	@Override
	public String getStatements(RenderContext info)
	{
		return getRealStatements(info).getStatements(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(getRealStatements(info));
	}

	protected JSStatements getRealStatements(RenderContext info)
	{
		JSStatements statements = info.getAttribute(this);
		if( statements == null )
		{
			statements = createStatements(info);
			info.setAttribute(this, statements);
		}
		return statements;
	}

	public void setStatements(SectionInfo info, JSStatements statements)
	{
		info.setAttribute(this, statements);
	}

	protected JSStatements createStatements(RenderContext info)
	{
		throw new SectionsRuntimeException("Statements not set and createStatements() not overridden"); //$NON-NLS-1$

	}
}
