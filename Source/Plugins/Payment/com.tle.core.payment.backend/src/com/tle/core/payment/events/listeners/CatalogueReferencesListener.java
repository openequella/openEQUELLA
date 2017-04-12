package com.tle.core.payment.events.listeners;

import java.util.List;

import com.tle.common.payment.entity.Catalogue;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Aaron
 */
public interface CatalogueReferencesListener extends ApplicationListener
{
	void addCatalogueReferencingClasses(Catalogue catalogue, List<Class<?>> referencingClasses);
}
