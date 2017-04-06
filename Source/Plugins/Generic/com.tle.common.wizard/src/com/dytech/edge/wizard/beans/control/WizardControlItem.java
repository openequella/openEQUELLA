/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

import java.io.Serializable;

import com.tle.beans.entity.LanguageBundle;

public class WizardControlItem implements Serializable
{
	private static final long serialVersionUID = 1;

	private LanguageBundle name;
	private String value;
	private String defaultValue;

	public WizardControlItem()
	{
		this(null, "", "");
	}

	public WizardControlItem(LanguageBundle name, String value)
	{
		this(name, value, "");
	}

	public WizardControlItem(LanguageBundle name, String value, String defaultValue)
	{
		this.name = name;
		this.value = value;
		this.defaultValue = defaultValue;
	}

	public String getDefault()
	{
		return defaultValue;
	}

	public void setDefault(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public boolean isDefaultOption()
	{
		return Boolean.parseBoolean(defaultValue);
	}

	public void setDefaultOption(boolean b)
	{
		defaultValue = Boolean.toString(b);
	}
}
