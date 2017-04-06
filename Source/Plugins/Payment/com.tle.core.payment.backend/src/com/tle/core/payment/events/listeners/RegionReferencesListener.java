package com.tle.core.payment.events.listeners;

import java.util.List;

import com.tle.common.payment.entity.Region;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface RegionReferencesListener extends ApplicationListener
{
	void addRegionReferencingClasses(Region region, List<Class<?>> referencingClasses);
}
