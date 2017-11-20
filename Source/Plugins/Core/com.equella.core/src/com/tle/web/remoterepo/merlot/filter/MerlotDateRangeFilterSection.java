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

package com.tle.web.remoterepo.merlot.filter;

import javax.inject.Inject;

import com.tle.common.util.TleDate;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.remoterepo.merlot.MerlotRemoteRepoSearchEvent;
import com.tle.web.remoterepo.merlot.MerlotWebService;
import com.tle.web.remoterepo.section.RemoteRepoDateRangeFilterSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
public class MerlotDateRangeFilterSection extends RemoteRepoDateRangeFilterSection
{
	@PlugKey("filter.date.title")
	private static Label TITLE;

	@Inject
	private MerlotWebService merlotWebService;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !merlotWebService.getSettings(context).isAdvancedApi() )
		{
			return null;
		}
		return super.renderHtml(context);
	}

	@Override
	protected Label getTitle(SectionInfo info)
	{
		return TITLE;
	}

	@Override
	protected void processDateRange(SectionInfo info, RemoteRepoSearchEvent event, TleDate[] dateRange)
	{
		final MerlotRemoteRepoSearchEvent merlot = (MerlotRemoteRepoSearchEvent) event;
		merlot.setCreatedAfter(dateRange[0]);
		merlot.setCreatedBefore(dateRange[1]);
	}

}
