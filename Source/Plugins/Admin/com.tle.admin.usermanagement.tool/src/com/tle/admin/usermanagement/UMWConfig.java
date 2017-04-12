/*
 * Created on Mar 18, 2005
 */

package com.tle.admin.usermanagement;

import org.java.plugin.registry.Extension;

public class UMWConfig extends UMPConfig
{
	private boolean enabled;
	private boolean visible;

	public UMWConfig(String className, String settingsClass, String name, int width, int height, Extension extension)
	{
		super(className, settingsClass, name, width, height, extension);
		visible = true;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
