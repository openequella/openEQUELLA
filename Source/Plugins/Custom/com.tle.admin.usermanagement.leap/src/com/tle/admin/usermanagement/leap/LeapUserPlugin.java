package com.tle.admin.usermanagement.leap;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.leap.wrapper.LeapUserWrapperSettings;

/**
 * @author aholland
 */
public class LeapUserPlugin extends GeneralPlugin<LeapUserWrapperSettings>
{
	private final LeapUserSettingsPanel generalPanel;

	public LeapUserPlugin()
	{
		generalPanel = new LeapUserSettingsPanel();

		setup();
	}

	protected void setup()
	{
		addFillComponent(generalPanel);
	}

	@Override
	public void load(LeapUserWrapperSettings settings)
	{
		generalPanel.load(settings);
	}

	@Override
	public boolean save(LeapUserWrapperSettings settings) throws EditorException
	{
		generalPanel.save(settings);
		return true;
	}
}
