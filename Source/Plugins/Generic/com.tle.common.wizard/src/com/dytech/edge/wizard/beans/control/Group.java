/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class Group extends WizardControl
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "group";
	private static final String CHECKBOX = "checkbox";

	private String type;
	private List<GroupItem> groups;
	@Deprecated
	@XStreamOmitField
	@SuppressWarnings("unused")
	private transient boolean compact;

	public Group()
	{
		groups = new ArrayList<GroupItem>();
	}

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public List<GroupItem> getGroups()
	{
		return groups;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean isCheckbox()
	{
		return CHECKBOX.equals(type);
	}

	public boolean isMultiselect()
	{
		return isCheckbox();
	}

	public void setMultiselect(boolean b)
	{
		type = b ? CHECKBOX : "radio";
	}
}
