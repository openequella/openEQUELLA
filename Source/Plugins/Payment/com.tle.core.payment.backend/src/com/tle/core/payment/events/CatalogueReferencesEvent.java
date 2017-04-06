package com.tle.core.payment.events;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Catalogue;
import com.tle.core.events.BaseEntityReferencesEvent;
import com.tle.core.payment.events.listeners.CatalogueReferencesListener;

/**
 * @author Aaron
 */
public class CatalogueReferencesEvent extends BaseEntityReferencesEvent<Catalogue, CatalogueReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public CatalogueReferencesEvent(Catalogue catalogue)
	{
		super(catalogue);
	}

	@Override
	public Class<CatalogueReferencesListener> getListener()
	{
		return CatalogueReferencesListener.class;
	}

	@Override
	public void postEvent(CatalogueReferencesListener listener)
	{
		listener.addCatalogueReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
