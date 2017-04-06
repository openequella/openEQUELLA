package com.tle.core.reporting.guice;

import com.tle.core.reporting.web.GenerateReportsAction;
import com.tle.core.reporting.web.ReportStreamingService;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ReportingModule extends SectionsModule
{
	@Override
	@SuppressWarnings("nls")
	protected void configure()
	{
		bindNamed("/access/reports", node(GenerateReportsAction.class));
		bindNamed("/services/reportingstream", node(ReportStreamingService.class));
	}
}
