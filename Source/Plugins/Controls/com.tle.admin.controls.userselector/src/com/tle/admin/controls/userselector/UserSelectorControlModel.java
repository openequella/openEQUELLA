package com.tle.admin.controls.userselector;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.wizard.controls.userselector.UserSelectorControl;

public class UserSelectorControlModel extends CustomControlModel<UserSelectorControl>
{
	public UserSelectorControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		return Validation.hasTarget(getControl());
	}
}
