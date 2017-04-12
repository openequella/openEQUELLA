package com.tle.web.settings.menu;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;

@SuppressWarnings("nls")
public final class SettingsUtils
{
	private static final KeyLabel BREADCRUMB_LABEL = new KeyLabel(ResourcesService.getResourceHelper(
		SettingsUtils.class).key("breadcrumb"));

	public static final SimpleBookmark SETTINGS_BOOKMARK = new SimpleBookmark("access/settings.do");

	private static final KeyLabel BREADCRUMB_TITLE = new KeyLabel(ResourcesService.getResourceHelper(
		SettingsUtils.class).key("breadcrumb.title"));

	public static HtmlLinkState getBreadcrumb()
	{
		HtmlLinkState link = new HtmlLinkState(SETTINGS_BOOKMARK);
		link.setLabel(BREADCRUMB_LABEL);
		link.setTitle(BREADCRUMB_TITLE);
		return link;
	}

	private SettingsUtils()
	{
		throw new Error();
	}
}
