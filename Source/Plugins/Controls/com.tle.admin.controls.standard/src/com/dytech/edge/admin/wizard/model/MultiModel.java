/*
 * Created on Apr 22, 2005
 */
package com.dytech.edge.admin.wizard.model;

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.Multi;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class MultiModel extends AbstractControlModel<Multi>
{
	private Multi multi;

	/**
	 * Constructs a new MultiModel.
	 */
	public MultiModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.multi = (Multi) wrappedObject;
	}

	@Override
	public List<?> getChildObjects()
	{
		return multi.getControls();
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public String getTargetBase()
	{
		String base = super.getTargetBase();
		if( !multi.getTargetnodes().isEmpty() )
		{
			base += multi.getTargetnodes().get(0).getTarget();
		}
		return base;
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList("multi");
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null )
		{
			error = Validation.hasChildren(this);
		}

		return error;
	}
}
