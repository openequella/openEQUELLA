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
