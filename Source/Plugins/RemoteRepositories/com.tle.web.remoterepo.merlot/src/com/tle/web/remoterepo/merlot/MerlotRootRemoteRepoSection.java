package com.tle.web.remoterepo.merlot;

import javax.inject.Inject;

import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.ContentLayout;

@SuppressWarnings("nls")
public class MerlotRootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "merlotRepoContext";

	@Inject
	private ViewMerlotResultSection viewResult;

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}

	@Override
	protected RemoteRepoViewResultSection<?, ?, ?> getViewSection()
	{
		return viewResult;
	}
}
