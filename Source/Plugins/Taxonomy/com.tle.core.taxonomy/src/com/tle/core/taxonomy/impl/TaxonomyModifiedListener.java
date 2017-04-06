package com.tle.core.taxonomy.impl;

import com.tle.core.events.listeners.ApplicationListener;

public interface TaxonomyModifiedListener extends ApplicationListener
{
	void taxonomyModifiedEvent(TaxonomyModifiedEvent event);
}
