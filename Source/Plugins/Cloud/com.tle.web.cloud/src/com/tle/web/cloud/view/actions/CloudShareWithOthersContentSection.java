package com.tle.web.cloud.view.actions;

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
import com.tle.web.cloud.view.section.CloudItemSectionInfo;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.viewitem.sharing.AbstractShareWithOthersSection;

@SuppressWarnings("nls")
@Bind
public class CloudShareWithOthersContentSection extends AbstractShareWithOthersSection
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private CloudViewItemLinkFactory linkFactory;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("actions/dialog/sharecloudwithothers.ftl", this);
	}

	@Override
	protected String createEmail(SectionInfo info)
	{
		return buildEmail(info);
	}

	private String buildEmail(SectionInfo info)
	{
		// TODO this could be further refactored
		StringBuilder email = new StringBuilder();
		CloudItemSectionInfo iinfo = CloudItemSectionInfo.getItemInfo(info);

		email.append(s("intro", getUser(CurrentUser.getDetails())));

		email.append(messageField.getValue(info));
		email.append("\n\n");
		email.append(s("item.name", CurrentLocale.get(iinfo.getViewableItem().getItem().getName())));
		email.append(s("item.link", linkFactory.createCloudViewLink(iinfo.getItemId()).getHref()));
		email.append(s("item.version", iinfo.getViewableItem().getItem().getVersion()));
		email.append("\n");
		email.append(s("outro"));

		return email.toString();
	}

}
