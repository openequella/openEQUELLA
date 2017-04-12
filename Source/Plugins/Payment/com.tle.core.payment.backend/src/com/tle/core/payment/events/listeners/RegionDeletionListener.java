package com.tle.core.payment.events.listeners;

import com.tle.common.payment.entity.Region;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface RegionDeletionListener extends ApplicationListener
{
	void removeRegionReferences(Region region);
}
