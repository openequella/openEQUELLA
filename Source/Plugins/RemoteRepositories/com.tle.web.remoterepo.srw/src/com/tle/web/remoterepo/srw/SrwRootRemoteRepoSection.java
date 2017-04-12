package com.tle.web.remoterepo.srw;

import javax.inject.Inject;

import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.ContentLayout;

@SuppressWarnings("nls")
public class SrwRootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "srwRepoContext";

	@Inject
	private ViewSrwResultSection viewSection;

	@Override
	protected RemoteRepoViewResultSection<?, ?, ?> getViewSection()
	{
		return viewSection;
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	@Override
	protected String getContentBodyClasses()
	{
		return "repo-onecol";
	}

	@Override
	protected String getSessionKey()
	{
		return CONTEXT_KEY;
	}
}
