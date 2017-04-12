package com.tle.core.payment.events;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.events.BaseEntityReferencesEvent;
import com.tle.core.payment.events.listeners.TaxTypeReferencesListener;

public class TaxTypeReferencesEvent extends BaseEntityReferencesEvent<TaxType, TaxTypeReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public TaxTypeReferencesEvent(TaxType taxType)
	{
		super(taxType);
	}

	@Override
	public Class<TaxTypeReferencesListener> getListener()
	{
		return TaxTypeReferencesListener.class;
	}

	@Override
	public void postEvent(TaxTypeReferencesListener listener)
	{
		listener.addTaxTypeReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
