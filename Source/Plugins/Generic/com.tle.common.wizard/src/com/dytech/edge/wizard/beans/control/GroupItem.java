/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

import com.tle.beans.entity.LanguageBundle;

public class GroupItem extends AbstractControlsWizardControl
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "groupitem";

	private LanguageBundle name;
	private String value;

	@Override
	public String getClassType()
	{
		return CLASS; // Should never be needed
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
}
