package com.tle.core.payment.events.listeners;

import com.tle.common.payment.entity.Catalogue;
import com.tle.core.events.listeners.ApplicationListener;

public interface CatalogueDeletionListener extends ApplicationListener
{
	void removeCatalogueReferences(Catalogue catalogue);
}
