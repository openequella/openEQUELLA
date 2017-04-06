package com.tle.core.payment.events.listeners;

import java.util.List;

import com.tle.common.payment.entity.TaxType;
import com.tle.core.events.listeners.ApplicationListener;

public interface TaxTypeReferencesListener extends ApplicationListener
{
	void addTaxTypeReferencingClasses(TaxType taxType, List<Class<?>> referencingClasses);
}
