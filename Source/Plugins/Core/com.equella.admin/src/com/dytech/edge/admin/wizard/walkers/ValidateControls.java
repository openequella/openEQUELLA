/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
