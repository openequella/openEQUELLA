package com.tle.core.payment.beans.store.conversion;

import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreCheckoutItemBean;

/**
 * @author Aaron
 */
public interface StoreBeanDeserializer
{
	Sale convertCartBeanToSale(StoreFront storeFront, StoreCheckoutBean cart);

	SaleItem convertCartItemBeanToSaleItem(StoreCheckoutItemBean cartItem, Sale sale);
}
