package com.tle.core.dynacollection;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.entity.DynaCollection;
import com.tle.core.events.BaseEntityReferencesEvent;

public class DynaCollectionReferencesEvent
	extends
		BaseEntityReferencesEvent<DynaCollection, DynaCollectionReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public DynaCollectionReferencesEvent(DynaCollection dc)
	{
		super(dc);
	}

	@Override
	public Class<DynaCollectionReferencesListener> getListener()
	{
		return DynaCollectionReferencesListener.class;
	}

	@Override
	public void postEvent(DynaCollectionReferencesListener listener)
	{
		listener.addDynaCollectionReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
