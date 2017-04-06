package com.tle.admin.usermanagement.canvas;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;

/**
 * @author aholland
 */
public class CanvasPlugin extends GeneralPlugin<CanvasWrapperSettings>
{
	private final CanvasSettingsPanel generalPanel;

	public CanvasPlugin()
	{
		generalPanel = new CanvasSettingsPanel();

		setup();
	}

	protected void setup()
	{
		addFillComponent(generalPanel);
	}

	@Override
	public void load(CanvasWrapperSettings settings)
	{
		generalPanel.load(settings);
	}

	@Override
	public boolean save(CanvasWrapperSettings settings) throws EditorException
	{
		generalPanel.save(settings);
		return true;
	}
}
