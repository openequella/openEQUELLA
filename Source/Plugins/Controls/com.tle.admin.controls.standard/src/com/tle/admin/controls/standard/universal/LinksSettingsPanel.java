package com.tle.admin.controls.standard.universal;

import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class LinksSettingsPanel extends UniversalControlSettingPanel
{
	public LinksSettingsPanel()
	{
		super();
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.standard.links.settings.title";
	}

	@Override
	public void load(UniversalSettings state)
	{
		// Nothing to do
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing to do
	}

	@Override
	public void save(UniversalSettings state)
	{
		// Nothing to do
	}
}
