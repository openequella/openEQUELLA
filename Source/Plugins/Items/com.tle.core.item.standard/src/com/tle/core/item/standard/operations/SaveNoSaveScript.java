package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class SaveNoSaveScript extends SaveOperation
{
	private boolean noSaveScript;

	@AssistedInject
	protected SaveNoSaveScript(@Assisted boolean noSaveScript)
	{
		super();
		this.noSaveScript = noSaveScript;
		setNoSaveScript(noSaveScript);
	}

	@Override
	protected void runSaveScript()
	{
		if( noSaveScript )
		{
			return;
		}
		super.runSaveScript();
	}

}
