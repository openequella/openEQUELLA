package com.tle.common.wizard.controls.universal;

import com.dytech.edge.wizard.beans.control.CustomControl;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class UniversalControl extends CustomControl
{
	private static final long serialVersionUID = 1L;

	public UniversalControl()
	{
		setClassType("universal");
	}

	public UniversalControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}
}
