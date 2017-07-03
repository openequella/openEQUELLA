package com.tle.core.powersearch.event;

import com.tle.beans.entity.PowerSearch;
import com.tle.core.entity.event.BaseEntityDeletionEvent;
import com.tle.core.powersearch.event.listener.PowerSearchDeletionListener;

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
