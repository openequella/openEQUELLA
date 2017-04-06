package com.tle.admin.controls;

import java.io.Serializable;
import java.util.List;

/*
 * @author aholland
 */
public class ExportedControl implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String controlTypeId;
	private String pluginId;
	private Object wizardControl;
	private String version;
	private List<ExportedControl> children;

	public String getControlTypeId()
	{
		return controlTypeId;
	}

	public void setControlTypeId(String controlTypeId)
	{
		this.controlTypeId = controlTypeId;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public Object getWizardControl()
	{
		return wizardControl;
	}

	public void setWizardControl(Object wizardControl)
	{
		this.wizardControl = wizardControl;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public List<ExportedControl> getChildren()
	{
		return children;
	}

	public void setChildren(List<ExportedControl> children)
	{
		this.children = children;
	}
}
