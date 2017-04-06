package com.tle.core.dynacollection;

import java.util.List;

import com.tle.beans.entity.DynaCollection;
import com.tle.core.events.listeners.ApplicationListener;

public interface DynaCollectionReferencesListener extends ApplicationListener
{
	void addDynaCollectionReferencingClasses(DynaCollection dc, List<Class<?>> referencingClasses);
}
