/*
 * Created on Mar 18, 2005
 */
package com.tle.admin.usermanagement;

import org.java.plugin.registry.Extension;

import com.tle.admin.plugin.PluginSetting;

public class UMPConfig extends PluginSetting
{
	private String settingsClass;

	public UMPConfig(String className, String settingsClass, String name, int width, int height, Extension extension)
	{
		this.setPluginClass(className);
		this.setName(name);
		this.settingsClass = settingsClass;
		setExtension(extension);
		setWidth(width);
		setHeight(height);
	}

	public String getSettingsClass()
	{
		return settingsClass;
	}

	public void setSettingsClass(String settingsClass)
	{
		this.settingsClass = settingsClass;
	}

}
