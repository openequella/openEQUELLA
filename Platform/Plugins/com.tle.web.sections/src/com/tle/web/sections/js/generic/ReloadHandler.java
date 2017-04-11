package com.tle.web.sections.js.generic;

import com.tle.web.sections.js.generic.statement.ReloadStatement;

public class ReloadHandler extends OverrideHandler
{
	public ReloadHandler()
	{
		this(true);
	}

	public ReloadHandler(boolean validate)
	{
		super(new ReloadStatement(validate));
	}
}
