package com.tle.web.wizard.guice;

import com.tle.web.sections.equella.guice.EquellaModule;
import com.tle.web.wizard.render.WizardFreemarkerFactory;

public class WizardControlModule extends EquellaModule
{
	@Override
	protected void bindFreemarker()
	{
		if( isBindStandardFreemarker() )
		{
			super.bindFreemarker();
			bind(NamedWizardFreemarkerFactory.class).asEagerSingleton();
		}
		else
		{
			bind(WizardFreemarkerFactory.class).asEagerSingleton();
		}
	}

	protected boolean isBindStandardFreemarker()
	{
		return false;
	}

	public static class NamedWizardFreemarkerFactory extends WizardFreemarkerFactory
	{
		@SuppressWarnings("nls")
		public NamedWizardFreemarkerFactory()
		{
			setName("wizardFreemarkerFactory");
		}
	}
}
