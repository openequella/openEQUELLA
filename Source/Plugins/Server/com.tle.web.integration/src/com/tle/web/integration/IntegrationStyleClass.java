package com.tle.web.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.template.section.HtmlStyleClass;

@Bind
@Singleton
public class IntegrationStyleClass implements HtmlStyleClass
{
	@Inject
	private IntegrationService integrationService;

	@Override
	public String getStyleClass(SectionInfo info)
	{
		return integrationService.isInIntegrationSession(info) ? "integration" : null;
	}
}
