package com.tle.admin.controls.emailselector;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.wizard.controls.emailselector.EmailSelectorControl;

public class EmailSelectorControlModel extends CustomControlModel<EmailSelectorControl>
{
	public EmailSelectorControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null && getControl().isSelectMultiple() )
		{
			error = Validation.noAttributeTargets(getControl());
		}

		return error;
	}
}
