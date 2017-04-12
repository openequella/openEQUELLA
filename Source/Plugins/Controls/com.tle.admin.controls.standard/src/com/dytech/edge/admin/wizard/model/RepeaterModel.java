package com.dytech.edge.admin.wizard.model;

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.Repeater;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

public class RepeaterModel extends AbstractControlModel<Repeater>
{
	public RepeaterModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public List<?> getChildObjects()
	{
		return getControl().getControls();
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList(Contexts.CONTEXT_PAGE);
	}

	@Override
	public String getTargetBase()
	{
		String base = super.getTargetBase();
		if( !getTargets().isEmpty() )
		{
			base += getTargets().get(0);
		}
		return base;
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null )
		{
			error = Validation.noAttributeTargets(getControl());
		}

		if( error == null )
		{
			error = Validation.hasChildren(this);
		}

		return error;
	}
}
