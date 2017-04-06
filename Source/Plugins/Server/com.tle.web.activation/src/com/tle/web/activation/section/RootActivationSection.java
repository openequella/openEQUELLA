package com.tle.web.activation.section;

import javax.inject.Inject;

import com.tle.web.activation.ActivationsPrivilegeTreeProvider;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

public class RootActivationSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	public static final String ACTIVATIONURL = "/access/activations.do"; //$NON-NLS-1$

	@Inject
	private ActivationsPrivilegeTreeProvider securityProvider;

	@PlugKey("title")
	private static Label title;

	@Override
	protected String getSessionKey()
	{
		return "activationContext"; //$NON-NLS-1$
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return title;
	}

	@SuppressWarnings("nls")
	@Override
	protected String getContentBodyClasses()
	{
		return super.getContentBodyClasses() + " activations-layout";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		return super.renderHtml(context);
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.TWO_COLUMN;
	}
}
