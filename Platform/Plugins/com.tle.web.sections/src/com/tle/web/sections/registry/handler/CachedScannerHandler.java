package com.tle.web.sections.registry.handler;

import com.tle.common.util.CachedClassData;
import com.tle.web.sections.RegistrationHandler;
import com.tle.web.sections.SectionTree;

public abstract class CachedScannerHandler<T> extends CachedClassData<T> implements RegistrationHandler
{
	@Override
	public void treeFinished(SectionTree tree)
	{
		// do nothing
	}
}
