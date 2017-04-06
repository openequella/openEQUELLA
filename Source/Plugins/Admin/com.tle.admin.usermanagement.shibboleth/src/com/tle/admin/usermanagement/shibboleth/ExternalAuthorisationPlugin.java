package com.tle.admin.usermanagement.shibboleth;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings;

/**
 * @author aholland
 */
public class ExternalAuthorisationPlugin extends GeneralPlugin<ExternalAuthorisationWrapperSettings>
{
	private final ExternalAuthorisationSettingsPanel generalPanel;

	public ExternalAuthorisationPlugin()
	{
		generalPanel = new ExternalAuthorisationSettingsPanel();

		setup();
	}

	protected void setup()
	{
		addFillComponent(generalPanel);
	}

	@Override
	public void load(ExternalAuthorisationWrapperSettings settings)
	{
		generalPanel.load(settings);
	}

	@Override
	public boolean save(ExternalAuthorisationWrapperSettings settings) throws EditorException
	{
		generalPanel.save(settings);
		return true;
	}
}
