package com.dytech.edge.admin.wizard.model;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.admin.controls.repository.ControlDefinition;

public class CustomControlModel<T extends CustomControl> extends AbstractControlModel<T>
{
	public CustomControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		CustomControl newWrapped = (CustomControl) getControlRepository().getNewWrappedObject(getDefinition().getId());
		((CustomControl) wrappedObject).cloneTo(newWrapped);
		super.setWrappedObject(newWrapped);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getWrappedObject()
	{
		return (T) super.getWrappedObject();
	}

	@Override
	public Object save()
	{
		return new CustomControl(getWrappedObject());
	}
}
