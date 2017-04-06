package com.tle.admin.controls.externaltools.universal;

import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.wizard.controls.universal.UniversalSettings;

@SuppressWarnings("nls")
public class LTISettingsPanel extends UniversalControlSettingPanel
{

	public LTISettingsPanel()
	{
		super();
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.admin.controls.externaltools.settings.title";
	}

	@Override
	public void load(UniversalSettings state)
	{
		// Nothing
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing
	}

	@Override
	public void save(UniversalSettings state)
	{
		// Nothing
	}
}

