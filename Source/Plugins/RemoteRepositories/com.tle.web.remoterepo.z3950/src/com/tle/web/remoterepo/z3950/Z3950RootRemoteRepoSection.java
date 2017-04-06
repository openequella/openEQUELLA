package com.tle.web.remoterepo.z3950;

import javax.inject.Inject;

import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.ContentLayout;

@SuppressWarnings("nls")
public class Z3950RootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "z3950RepoContext";

	@Inject
	private ViewZ3950ResultSection viewSection;

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
