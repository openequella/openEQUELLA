package com.tle.core.payment.events;

import com.tle.common.payment.entity.Region;
import com.tle.core.events.BaseEntityDeletionEvent;
import com.tle.core.payment.events.listeners.RegionDeletionListener;

/**
 * @author Aaron
 */
public class RegionDeletionEvent extends BaseEntityDeletionEvent<Region, RegionDeletionListener>
{
	private static final long serialVersionUID = 1L;

	public RegionDeletionEvent(Region region)
	{
		super(region);
	}

	@Override
	public Class<RegionDeletionListener> getListener()
	{
		return RegionDeletionListener.class;
	}

	@Override
	public void postEvent(RegionDeletionListener listener)
	{
		listener.removeRegionReferences(entity);
	}
}
