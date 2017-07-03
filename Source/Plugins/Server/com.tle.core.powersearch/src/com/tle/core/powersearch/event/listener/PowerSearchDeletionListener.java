package com.tle.core.powersearch.event.listener;

import com.tle.beans.entity.PowerSearch;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public interface PowerSearchDeletionListener extends ApplicationListener
{
	void removeReferences(PowerSearch powerSearch);
}
