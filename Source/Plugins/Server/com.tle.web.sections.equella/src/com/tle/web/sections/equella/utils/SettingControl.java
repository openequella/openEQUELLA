package com.tle.web.sections.equella.utils;

import com.tle.web.sections.SectionId;

public class SettingControl
{
	private String label;
	private SectionId component;
	private String error;
	private String help;
	private boolean mandatory;

	public SettingControl(String label, SectionId component, String error, String help, boolean mandatory)
	{
		this.label = label;
		this.component = component;
		this.error = error;
		this.help = help;
		this.mandatory = mandatory;
	}

	public SettingControl(String label, SectionId component)
	{
		this(label, component, null, null, false);
	}

	public SettingControl(String label, SectionId component, boolean mandatory)
	{
		this(label, component, null, null, mandatory);
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public SectionId getComponent()
	{
		return component;
	}

	public void setComponent(SectionId component)
	{
		this.component = component;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String errro)
	{
		this.error = errro;
	}

	public String getHelp()
	{
		return help;
	}

	public void setHelp(String help)
	{
		this.help = help;
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}
}
