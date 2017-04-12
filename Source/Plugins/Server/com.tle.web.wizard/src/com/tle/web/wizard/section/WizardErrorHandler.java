package com.tle.web.wizard.section;

import javax.inject.Singleton;

import com.dytech.edge.wizard.WizardException;
import com.google.common.base.Throwables;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;

@Bind
@Singleton
public class WizardErrorHandler implements SectionsExceptionHandler
{

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		return ex instanceof WizardException;
	}

	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		if( exception instanceof WizardException )
		{
			WizardException we = (WizardException) exception;
			Throwable cause = we.getCause();
			if( cause == null )
			{
				cause = we;
			}
			RootWizardSection rootWizard = info.lookupSection(RootWizardSection.class);
			rootWizard.setException(info, cause);
			info.preventGET();
			info.renderNow();
		}
		else
		{
			Throwables.propagate(exception);
		}
	}

}
