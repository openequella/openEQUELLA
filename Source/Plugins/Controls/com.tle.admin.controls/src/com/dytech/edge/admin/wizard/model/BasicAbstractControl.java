package com.dytech.edge.admin.wizard.model;

import java.util.List;

import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.LanguageBundle;

public abstract class BasicAbstractControl extends Control
{

	public BasicAbstractControl(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public List<?> getChildObjects()
	{
		return null;
	}

	@Override
	public String getCustomName()
	{
		return null;
	}

	@Override
	public String getScript()
	{
		return null;
	}

	@Override
	public LanguageBundle getTitle()
	{
		return null;
	}

	@Override
	public boolean isPowerSearchInclude()
	{
		return false;
	}

	@Override
	public void setCustomName(String string)
	{
		// nothing
	}

	@Override
	public void setPowerSearchInclude(boolean b)
	{
		// nothing
	}

	@Override
	public void setScript(String script)
	{
		// nothing
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
