package com.tle.core.payment.events;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Region;
import com.tle.core.events.BaseEntityReferencesEvent;
import com.tle.core.payment.events.listeners.RegionReferencesListener;

/**
 * @author Aaron
 */
public class RegionReferencesEvent extends BaseEntityReferencesEvent<Region, RegionReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public RegionReferencesEvent(Region region)
	{
		super(region);
	}

	@Override
	public Class<RegionReferencesListener> getListener()
	{
		return RegionReferencesListener.class;
	}

	@Override
	public void postEvent(RegionReferencesListener listener)
	{
		listener.addRegionReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
