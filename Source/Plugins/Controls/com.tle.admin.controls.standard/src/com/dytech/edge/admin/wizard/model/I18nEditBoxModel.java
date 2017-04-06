package com.dytech.edge.admin.wizard.model;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.MultiEditBox;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

public class I18nEditBoxModel extends AbstractControlModel<MultiEditBox>
{
	public I18nEditBoxModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null )
		{
			error = Validation.noAttributeTargets(getControl());
		}

		return error;
	}
}
