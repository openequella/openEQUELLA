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

package com.tle.web.notification.section;

import javax.inject.Inject;

import com.tle.web.navigation.TopbarLinkService;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class RootNotificationListSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	@PlugKey("title")
	private static Label LABEL_TITLE;

	@SuppressWarnings("nls")
	public static final String URL = "/access/notifications.do";

	@Inject
	private TopbarLinkService topbarLinkService;

	@Override
	protected String getSessionKey()
	{
		return URL;
	}


	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@DirectEvent
	public void updateTopbar(SectionInfo info)
	{
		topbarLinkService.clearCachedData();
	}

}
