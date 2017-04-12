package com.dytech.edge.admin.wizard.model;

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.Group;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

public class GroupModel extends AbstractControlModel<Group>
{
	private Group group;

	public GroupModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.group = (Group) wrappedObject;
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null && getControl().isMultiselect() )
		{
			error = Validation.noAttributeTargets(getControl());
		}

		if( error == null )
		{
			error = Validation.hasChildren(this);
		}

		return error;
	}

	@Override
	public List<?> getChildObjects()
	{
		return group.getGroups();
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList(Contexts.CONTEXT_GROUP);
	}
}
