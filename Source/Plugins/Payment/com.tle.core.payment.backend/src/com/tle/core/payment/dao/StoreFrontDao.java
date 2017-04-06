package com.tle.core.payment.dao;

import java.util.List;

import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.dao.AbstractEntityDao;

public interface StoreFrontDao extends AbstractEntityDao<StoreFront>
{
	StoreFront getByOAuthClient(OAuthClient client);

	List<StoreFront> enumerateForTaxType(TaxType taxType);

	Long countSalesForStoreFront(StoreFront storeFront);
}
