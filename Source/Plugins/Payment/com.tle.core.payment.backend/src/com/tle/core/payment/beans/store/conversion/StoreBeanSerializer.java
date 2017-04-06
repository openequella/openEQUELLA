package com.tle.core.payment.beans.store.conversion;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.base.Function;
import com.tle.beans.item.Item;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

/**
 * @author Aaron
 */
public interface StoreBeanSerializer
{
	/**
	 * Does not apply prices!
	 * 
	 * @param tier
	 * @param perUser
	 * @return
	 */
	StorePurchaseTierBean convertPurchaseTierToBean(PricingTier tier, boolean perUser);

	StorePurchaseTierBean convertPurchaseTierToBean(PricingTier tier, boolean perUser,
		Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes);

	/**
	 * Does not apply prices!
	 * 
	 * @param tier
	 * @param perUser
	 * @return
	 */
	StoreSubscriptionTierBean convertSubscriptionTierToBean(PricingTier tier, boolean perUser);

	StoreSubscriptionTierBean convertSubscriptionTierToBean(PricingTier tier, boolean perUser,
		Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes);

	StoreCatalogueItemBean convertItemBeanToCatalogueItemBean(EquellaItemBean item, String catalogueUuid, boolean free,
		PricingTier pt, PricingTier st, Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes);

	StoreHarvestableItemBean convertItemToHarvestableItemBean(Item item);

	StoreCatalogueBean convertCatalogueToBean(Catalogue catalogue);

	StorePriceBean convertPriceToBean(Price price, BigDecimal tax, List<TaxType> taxes);

	StoreSubscriptionPeriodBean convertSubscriptionPeriodToBean(SubscriptionPeriod period);

	StorePaymentGatewayBean convertPaymentGatewayToBean(PaymentGateway pg);

	/**
	 * More or less the same as the one below
	 * 
	 * @param sale
	 * @return
	 */
	StoreCheckoutBean convertSaleToCheckoutBean(Sale sale);

	StoreTransactionBean convertSaleToTransactionBean(Sale sale);

	StoreHarvestableItemBean convertSaleItemToStoreHarvestableItemBean(SaleItem saleItem);
}
