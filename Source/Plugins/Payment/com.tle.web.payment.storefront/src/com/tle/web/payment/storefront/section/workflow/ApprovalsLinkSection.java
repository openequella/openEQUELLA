package com.tle.web.payment.storefront.section.workflow;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.privileges.ApprovalsSettingsPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
public class ApprovalsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("approvals.title")
	private static Label TITLE_LABEL;
	@PlugKey("approvals.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private ApprovalsSettingsPrivilegeTreeProvider securityProvider;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised();
	}

	@SuppressWarnings("nls")
	public static HtmlLinkState getShowApprovalsLink(SectionInfo info)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = info.createForward("/access/approvals.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		return new Pair<HtmlLinkState, Label>(getShowApprovalsLink(context), DESCRIPTION_LABEL);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
