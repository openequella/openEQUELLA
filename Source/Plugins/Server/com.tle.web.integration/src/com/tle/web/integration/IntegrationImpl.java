/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
