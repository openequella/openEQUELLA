package com.dytech.edge.admin.wizard.model;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

public class CalendarModel extends AbstractControlModel<Calendar>
{
	public CalendarModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null && getControl().isRange() )
		{
			error = Validation.noAttributeTargets(getControl());
		}

		return error;
	}
}
