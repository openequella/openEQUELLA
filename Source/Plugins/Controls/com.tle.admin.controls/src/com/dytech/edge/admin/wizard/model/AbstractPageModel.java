package com.dytech.edge.admin.wizard.model;

import java.util.Collections;
import java.util.List;

import com.dytech.edge.wizard.beans.WizardPage;
import com.tle.admin.controls.repository.ControlDefinition;

public abstract class AbstractPageModel<T extends WizardPage> extends Control
{
	protected T page;

	public AbstractPageModel(ControlDefinition definition)
	{
		super(definition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.page = (T) wrappedObject;
	}

	@Override
	public String getControlClass()
	{
		return page.getType();
	}

	@Override
	public String getScript()
	{
		return page.getScript();
	}

	@Override
	public boolean isPowerSearchInclude()
	{
		return false;
	}

	@Override
	public void setScript(String script)
	{
		page.setScript(script);
	}

	@Override
	public List<String> getTargets()
	{
		return Collections.emptyList();
	}

	@Override
	public void setPowerSearchInclude(boolean b)
	{
		// nothing;
	}

	@Override
	public void setCustomName(String string)
	{
		// nothing
	}

	@Override
	public String getCustomName()
	{
		return null;
	}

	@Override
	public Object save()
	{
		return page;
	}

	public T getPage()
	{
		return page;
	}

	@Override
	public boolean isRemoveable()
	{
		return true;
	}

	@Override
	public boolean isScriptable()
	{
		return true;
	}
}
