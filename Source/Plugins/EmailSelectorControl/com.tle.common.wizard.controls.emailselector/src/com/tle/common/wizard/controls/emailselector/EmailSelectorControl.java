package com.tle.common.wizard.controls.emailselector;

import com.dytech.edge.wizard.beans.control.CustomControl;

public class EmailSelectorControl extends CustomControl
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_SELECT_MULTIPLE = "IsSelectMultiple"; //$NON-NLS-1$

	public EmailSelectorControl()
	{
		setClassType("emailselector"); //$NON-NLS-1$
	}

	public EmailSelectorControl(CustomControl cloned)
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
