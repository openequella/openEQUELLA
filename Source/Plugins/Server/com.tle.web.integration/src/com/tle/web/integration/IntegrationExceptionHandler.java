package com.tle.web.integration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.errors.DefaultExceptionHandler;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.template.Decorations;

@Bind
@Singleton
public class IntegrationExceptionHandler extends DefaultExceptionHandler
{
	@Inject
	private IntegrationService integrationService;

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		if( integrationService.getIntegrationInterface(info) != null )
		{
			return true;
		}
		return false;
	}

	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		if( checkRendered(info) )
		{
			return;
		}
		SectionInfo newInfo = createNewInfo(exception, info, controller);
		Decorations.getDecorations(newInfo).clearAllDecorations();
		controller.execute(newInfo);
	}
}
