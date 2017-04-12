package com.tle.web.customlinks.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

public class RootCustomLinksSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setContentBodyClass("customlinks"); //$NON-NLS-1$
		decorations.setOptions(false);

		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}
}
