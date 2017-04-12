package com.tle.admin.gui.common;

public class EmptyDynamicChoicePanel<STATE_TYPE> extends DynamicChoicePanel<STATE_TYPE>
{
	@Override
	public void load(STATE_TYPE state)
	{
		// Nothing to do here
	}

	@Override
	public void removeSavedState(STATE_TYPE state)
	{
		// Nothing to do here
	}

	@Override
	public void save(STATE_TYPE state)
	{
		// Nothing to do here
	}
}