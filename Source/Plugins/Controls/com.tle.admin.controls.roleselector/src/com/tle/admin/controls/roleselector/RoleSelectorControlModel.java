package com.tle.admin.controls.roleselector;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.wizard.controls.roleselector.RoleSelectorControl;

public class RoleSelectorControlModel extends CustomControlModel<RoleSelectorControl>
{
	public RoleSelectorControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		return Validation.hasTarget(getControl());
	}
}
