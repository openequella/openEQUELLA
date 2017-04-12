package com.tle.web.wizard.guice;

public class WizardBindBothModule extends WizardControlModule
{
	@Override
	protected boolean isBindStandardFreemarker()
	{
		return true;
	}
}
