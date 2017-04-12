package com.tle.admin.taxonomy.wizard;

import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class WidePopupBrowserConfig extends PopupBrowserConfig
{
	@Override
	protected String getDescription()
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.widePopupBrowser.description");
	}
}
