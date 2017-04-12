package com.tle.common.wizard.controls.roleselector;

import com.dytech.edge.wizard.beans.control.CustomControl;

@SuppressWarnings("nls")
public class RoleSelectorControl extends CustomControl
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_SELECT_MULTIPLE = "IsSelectMultiple";

	public RoleSelectorControl()
	{
		setClassType("roleselector");
	}

	public RoleSelectorControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}

	public boolean isSelectMultiple()
	{
		return getBooleanAttribute(KEY_SELECT_MULTIPLE);
	}

	public void setSelectMultiple(boolean multiple)
	{
		getAttributes().put(KEY_SELECT_MULTIPLE, multiple);
	}
}