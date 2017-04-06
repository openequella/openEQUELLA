/*
 * Created on May 3, 2005
 */
package com.dytech.edge.admin.wizard.walkers;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.Control;
import com.tle.common.applet.client.ClientService;

/**
 * @author Nicholas Read
 */
public class ValidateControls extends ControlTreeWalker
{
	private final ClientService clientService;

	private Control invalidControl;

	public ValidateControls(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public boolean errorDetected()
	{
		return invalidControl != null;
	}

	/**
	 * @return Returns the first invalid control, or null if they are all valid.
	 */
	public Control getInvalidControl()
	{
		return invalidControl;
	}

	@Override
	protected boolean onDescent(Control control)
	{
		boolean isValid = Validation.validateControl(control, clientService);
		if( !isValid )
		{
			invalidControl = control;
		}
		return isValid;
	}
}
