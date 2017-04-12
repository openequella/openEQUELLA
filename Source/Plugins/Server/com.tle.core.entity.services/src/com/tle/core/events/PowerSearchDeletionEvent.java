package com.tle.core.events;

import com.tle.beans.entity.PowerSearch;
import com.tle.core.events.listeners.PowerSearchDeletionListener;

/**
 * @author Nicholas Read
 */
public class PowerSearchDeletionEvent extends BaseEntityDeletionEvent<PowerSearch, PowerSearchDeletionListener>
{
	public PowerSearchDeletionEvent(PowerSearch powerSearch)
	{
		super(powerSearch);
	}

	@Override
	public Class<PowerSearchDeletionListener> getListener()
	{
		return PowerSearchDeletionListener.class;
	}

	@Override
	public void postEvent(PowerSearchDeletionListener listener)
	{
		listener.removeReferences(entity);
	}
}
