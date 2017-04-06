package com.tle.core.events.listeners;

import com.tle.beans.entity.PowerSearch;

/**
 * @author Nicholas Read
 */
public interface PowerSearchDeletionListener extends ApplicationListener
{
	void removeReferences(PowerSearch powerSearch);
}
