package com.dytech.edge.admin.wizard.model;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.RadioGroup;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

public class RadioButtonModel extends AbstractControlModel<RadioGroup>
{
	public RadioButtonModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		return Validation.hasTarget(getControl());
	}
}
