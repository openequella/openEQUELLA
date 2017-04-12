package com.tle.core.payment.service;

import com.tle.common.payment.entity.StoreFront;
import com.tle.core.payment.service.session.StoreFrontEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public interface StoreFrontService extends AbstractEntityService<StoreFrontEditingBean, StoreFront>
{
	String ENTITY_TYPE = "STOREFRONT";

	String FIELD_PRODUCT_NAME = "productName";
	String FIELD_PRODUCT_VERSION = "productVersion";
	String FIELD_CLIENT_ID = "clientId";
	String FIELD_CLIENT_ENTITY = "client";
	String FIELD_REDIRECT_URL = "redirectUrl";
	String FIELD_USER = "user";
	String FIELD_COUNTRY = "country";

	/**
	 * Determine if a store has seen any activity, to wit do any OrderStoreParts
	 * exist which reference this store.
	 * 
	 * @param boolean
	 * @return
	 */
	boolean storeFrontHasHistory(StoreFront storeFront);
}
