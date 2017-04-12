package com.tle.core.system.events;

import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public interface LicenseChangeListener extends ApplicationListener
{
	void licenseUpdated();
}
