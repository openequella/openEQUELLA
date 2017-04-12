package com.tle.core.payment.dao;

import java.util.List;

import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.payment.SaleSearchResults;

/**
 * @author aholland
 */
public interface SaleDao extends GenericInstitutionalDao<Sale, Long>
{
	Sale get(String uuid);

	Sale getByReceipt(String receipt);

	List<Sale> enumerateForStoreFront(StoreFront storeFront);

	SaleSearchResults search(StoreFront storeFront, int offset, int count, String customerReference);

	Sale getByCustomerRef(String refUuid);

	void deleteAll();
}
