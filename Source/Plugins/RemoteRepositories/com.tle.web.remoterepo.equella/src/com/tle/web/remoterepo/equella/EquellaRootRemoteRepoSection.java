package com.tle.web.remoterepo.equella;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.section.AbstractRootRemoteRepoSection;
import com.tle.web.remoterepo.section.RemoteRepoViewResultSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
@Bind
public class EquellaRootRemoteRepoSection extends AbstractRootRemoteRepoSection
{
	protected static final String CONTEXT_KEY = "equellaRepoContext";

	@Inject
	private EquellaRepoDownloadProgressSection progressSection;

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(progressSection, id);
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

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( progressSection.isShowing(context) )
		{
			setModalSection(context, progressSection);
		}
		return super.renderHtml(context);
	}

	@Override
	protected RemoteRepoViewResultSection<?, ?, ?> getViewSection()
	{
		return null;
	}

}
