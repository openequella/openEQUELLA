package com.tle.core.payment.events;

import com.tle.common.payment.entity.Catalogue;
import com.tle.core.events.BaseEntityDeletionEvent;
import com.tle.core.payment.events.listeners.CatalogueDeletionListener;

/**
 * @author Aaron
 */
public class CatalogueDeletionEvent extends BaseEntityDeletionEvent<Catalogue, CatalogueDeletionListener>
{
	private static final long serialVersionUID = 1L;

	public CatalogueDeletionEvent(Catalogue catalogue)
	{
		super(catalogue);
	}

	@Override
	public Class<CatalogueDeletionListener> getListener()
	{
		return CatalogueDeletionListener.class;
	}

	@Override
	public void postEvent(CatalogueDeletionListener listener)
	{
		listener.removeCatalogueReferences(entity);
	}
}
