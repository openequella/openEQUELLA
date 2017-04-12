package com.tle.web.api.payment.backend.links;

import com.tle.core.payment.beans.store.StoreCatalogueItemBean;

public interface StoreBeanLinkService
{
	void addLinks(StoreCatalogueItemBean storeItemBean, String catalogueUuid, boolean lite);
}
