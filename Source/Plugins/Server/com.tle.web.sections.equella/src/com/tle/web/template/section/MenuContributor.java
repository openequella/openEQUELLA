package com.tle.web.template.section;

import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;

public interface MenuContributor
{
	List<MenuContribution> getMenuContributions(SectionInfo info);

	void clearCachedData();

	class MenuContribution
	{
		private final HtmlLinkState link;
		private final String backgroundImagePath;
		private final int groupPriority;
		private final int linkPriority;

		public MenuContribution(HtmlLinkState link, String backgroundImage, int groupPriority, int linkPriority)
		{
			this.link = link;
			this.backgroundImagePath = backgroundImage;
			this.groupPriority = groupPriority;
			this.linkPriority = linkPriority;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		public String getBackgroundImagePath()
		{
			return backgroundImagePath;
		}

		public int getGroupPriority()
		{
			return groupPriority;
		}

		public int getLinkPriority()
		{
			return linkPriority;
		}
	}
}
