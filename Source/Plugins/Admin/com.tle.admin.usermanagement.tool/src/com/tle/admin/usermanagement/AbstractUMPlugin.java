package com.tle.admin.usermanagement;

import java.awt.Frame;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.plugin.PluginDialog;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.core.remoting.RemoteUserService;

public class AbstractUMPlugin extends PluginDialog<UserManagementSettings, UMPConfig>
{
	private static final long serialVersionUID = 1L;
	private UserManagementSettings settings;
	private RemoteUserService userService;

	public AbstractUMPlugin(Frame frame, String title, UMPConfig setting, GeneralPlugin<UserManagementSettings> plugin,
		RemoteUserService userService)
	{
		super(frame, title, setting, plugin);
		this.userService = userService;
	}

	@Override
	protected void _load(GeneralPlugin<UserManagementSettings> gplugin)
	{
		settings = userService.getPluginConfig(setting.getSettingsClass());
		gplugin.load(settings);
	}

	@Override
	protected void _save(GeneralPlugin<UserManagementSettings> gplugin) throws EditorException
	{
		if( gplugin.save(settings) )
		{
			userService.setPluginConfig(settings);
		}
	}
}
