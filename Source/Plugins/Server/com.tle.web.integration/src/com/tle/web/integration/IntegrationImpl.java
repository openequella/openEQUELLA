package com.tle.web.integration;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.selection.SelectionSession;

public class IntegrationImpl implements IntegrationInterface
{
	private final Integration<IntegrationSessionData> integrationService;
	private final IntegrationSessionData data;

	public IntegrationImpl(IntegrationSessionData data, Integration<IntegrationSessionData> integrationService)
	{
		this.data = data;
		this.integrationService = integrationService;
	}

	@Override
	public IntegrationSessionData getData()
	{
		return data;
	}

	@Override
	public String getClose()
	{
		return integrationService.getClose(data);
	}

	@Override
	public String getCourseInfoCode()
	{
		return integrationService.getCourseInfoCode(data);
	}

	@Override
	public NameValue getLocation()
	{
		return integrationService.getLocation(data);
	}

	@Override
	public LayoutSelector createLayoutSelector(SectionInfo info)
	{
		return integrationService.createLayoutSelector(info, data);
	}

	@Override
	public boolean select(SectionInfo info, SelectionSession session)
	{
		return integrationService.select(info, data, session);
	}

}
