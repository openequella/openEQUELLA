package com.tle.core.payment.beans.store.conversion.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.interfaces.SimpleI18NString;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.impl.AttachmentSerializerProvider;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.beans.store.DecimalNumberBean;
import com.tle.core.payment.beans.store.StoreCatalogueAttachmentBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.beans.store.StoreTransactionItemBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfo;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.item.ItemService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntitySerializer;
import com.tle.web.api.baseentity.serializer.BaseEntityEditor;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.item.equella.interfaces.beans.AbstractFileAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.equella.interfaces.beans.FileAttachmentBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.api.item.interfaces.beans.NavigationNodeBean;
import com.tle.web.api.item.interfaces.beans.NavigationTabBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;

/**
 * @author Aaron & Dustin a bit
 */
@Bind(StoreBeanSerializer.class)
@Singleton
public class StoreBeanSerializerImpl
	extends
		AbstractBaseEntitySerializer<BaseEntity, BaseEntityBean, BaseEntityEditor<BaseEntity, BaseEntityBean>>
	implements
		StoreBeanSerializer
{
	@Inject
	private PricingTierService tierService;
	@Inject
	private PaymentGatewayService paymentGatewayService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private AttachmentSerializerProvider attachmentSerializer;

	@Override
	protected BaseEntityBean createBean()
	{
		// Not currently used
		throw new Error();
	}

	@Override
	protected BaseEntity createEntity()
	{
		// Not currently used
		throw new Error();
	}

	@Override
	public BaseEntityBean serialize(BaseEntity entity, Object data, boolean heavy)
	{
		// Not currently used
		throw new Error();
	}

	@Override
	protected BaseEntityEditor<BaseEntity, BaseEntityBean> createExistingEditor(BaseEntity entity, String stagingUuid,
		String lockId, boolean importing)
	{
		throw new Error();
	}

	@Override
	protected BaseEntityEditor<BaseEntity, BaseEntityBean> createNewEditor(BaseEntity entity, String stagingUuid,
		boolean importing)
	{
		throw new Error();
	}

	@Override
	public StorePurchaseTierBean convertPurchaseTierToBean(PricingTier tier, boolean perUser)
	{
		if( tier == null )
		{
			return null;
		}
		final StorePurchaseTierBean bean = new StorePurchaseTierBean();
		copyBaseEntityFields(tier, bean, true);
		bean.setPerUser(perUser);
		return bean;
	}

	@Override
	public StorePurchaseTierBean convertPurchaseTierToBean(PricingTier tier, boolean perUser,
		Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes)
	{
		final StorePurchaseTierBean bean = convertPurchaseTierToBean(tier, perUser);
		if( bean == null )
		{
			return null;
		}
		final Price price = tierService.getPriceForPurchaseTier(tier);
		bean.setPrice(convertPriceToBean(price, taxCalculator.apply(price), taxes));
		return bean;
	}

	@Override
	public StoreSubscriptionTierBean convertSubscriptionTierToBean(PricingTier tier, boolean perUser)
	{
		if( tier == null )
		{
			return null;
		}
		final StoreSubscriptionTierBean bean = new StoreSubscriptionTierBean();
		copyBaseEntityFields(tier, bean, true);
		bean.setPerUser(perUser);
		return bean;
	}

	@Override
	public StoreSubscriptionTierBean convertSubscriptionTierToBean(PricingTier tier, boolean perUser,
		Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes)
	{
		final StoreSubscriptionTierBean bean = convertSubscriptionTierToBean(tier, perUser);
		if( bean == null )
		{
			return null;
		}
		final List<StorePriceBean> prices = Lists.newArrayList();
		final Map<SubscriptionPeriod, Price> priceMap = tierService.getPriceMapForSubscriptionTier(tier);
		for( Entry<SubscriptionPeriod, Price> entry : priceMap.entrySet() )
		{
			Price price = entry.getValue();
			prices.add(convertPriceToBean(price, taxCalculator.apply(price), taxes));
		}
		bean.setPrices(prices);
		return bean;
	}

	@Override
	public StoreCatalogueBean convertCatalogueToBean(Catalogue catalogue)
	{
		final StoreCatalogueBean bean = new StoreCatalogueBean();
		copyBaseEntityFields(catalogue, bean, true);
		return bean;
	}

	@Override
	public StorePriceBean convertPriceToBean(Price price, BigDecimal tax, List<TaxType> taxes)
	{
		final StorePriceBean bean = convertPrice(price.getValue(), tax, price.getCurrency());
		final SubscriptionPeriod period = price.getPeriod();
		if( period != null )
		{
			bean.setPeriod(convertSubscriptionPeriodToBean(period));
		}
		if( taxes != null )
		{
			final List<StoreTaxBean> taxBeans = Lists.newArrayList();
			for( TaxType taxType : taxes )
			{
				final StoreTaxBean taxBean = new StoreTaxBean();
				taxBean.setCode(taxType.getCode());
				taxBean.setRate(new DecimalNumberBean(taxType.getPercent()));
				taxBeans.add(taxBean);
			}
			bean.setTaxes(taxBeans);
		}
		else
		{
			bean.setTaxes(Collections.<StoreTaxBean> emptyList());
		}
		return bean;
	}

	@Override
	public StoreSubscriptionPeriodBean convertSubscriptionPeriodToBean(SubscriptionPeriod period)
	{
		final StoreSubscriptionPeriodBean bean = new StoreSubscriptionPeriodBean();
		bean.setUuid(period.getUuid());
		bean.setDuration(period.getDuration());
		bean.setDurationUnit(period.getDurationUnit().toString());
		bean.setNameFromBundle(period.getName(), period.getUuid());
		return bean;
	}

	@Override
	public StorePaymentGatewayBean convertPaymentGatewayToBean(PaymentGateway pg)
	{
		final StorePaymentGatewayBean pgb = new StorePaymentGatewayBean();
		copyBaseEntityFields(pg, pgb, true);

		pgb.setGatewayType(pg.getGatewayType());

		final PaymentGatewayCheckoutInfo checkoutInfo = paymentGatewayService.getCheckoutInfo(pg);
		if( checkoutInfo != null )
		{
			pgb.setCheckoutUrl(checkoutInfo.getCheckoutUrl());
			pgb.setButtonUrl(checkoutInfo.getButtonImageUrl());
		}

		return pgb;
	}

	@Override
	public StoreCheckoutBean convertSaleToCheckoutBean(Sale sale)
	{
		final StoreCheckoutBean scb = new StoreCheckoutBean();
		scb.setCreationDate(sale.getCreationDate());
		scb.setUuid(sale.getUuid());
		scb.setCustomerReference(sale.getCustomerReference());
		scb.setPrice(convertPrice(sale.getPrice(), sale.getTax(), sale.getCurrency()));

		final StoreCheckoutBean.PaidStatus status;
		switch( sale.getPaidStatus() )
		{
			case PAID:
				status = StoreCheckoutBean.PaidStatus.PAID;
				break;
			case PENDING:
				status = StoreCheckoutBean.PaidStatus.PENDING;
				break;
			case NONE:
				status = StoreCheckoutBean.PaidStatus.SUBMITTED;
				break;
			default:
				status = null;
		}
		scb.setPaidStatus(status);
		return scb;
	}

	@Override
	public StoreTransactionBean convertSaleToTransactionBean(Sale sale)
	{
		final Currency currency = sale.getCurrency();

		final StoreTransactionBean transactionBean = new StoreTransactionBean();
		transactionBean.setCreationDate(sale.getCreationDate());
		transactionBean.setUuid(sale.getUuid());
		transactionBean.setPrice(convertPrice(sale.getPrice(), sale.getTax(), currency));

		final StoreTransactionBean.PaidStatus status;
		switch( sale.getPaidStatus() )
		{
			case PAID:
				status = StoreTransactionBean.PaidStatus.PAID;
				break;
			case PENDING:
				status = StoreTransactionBean.PaidStatus.PENDING;
				break;
			case NONE:
				status = StoreTransactionBean.PaidStatus.SUBMITTED;
				break;
			default:
				status = null;
		}
		transactionBean.setPaidStatus(status);
		transactionBean.setPaidDate(sale.getPaidDate());
		transactionBean.setReceipt(sale.getReceipt());

		final PaymentSettings paymentSettings = configService.getProperties(new PaymentSettings());
		final boolean purchasePerUser = !paymentSettings.isPurchaseFlatRate();
		final boolean subPerUser = !paymentSettings.isSubscriptionFlatRate();

		final List<StoreTransactionItemBean> transactionItemBeans = new ArrayList<StoreTransactionItemBean>();
		for( SaleItem saleItem : sale.getSales() )
		{
			final StoreTransactionItemBean transactionItemBean = new StoreTransactionItemBean();
			transactionItemBean.setUuid(saleItem.getUuid());

			// very simplified item representation ...
			final StoreHarvestableItemBean itemBean = new StoreHarvestableItemBean();
			itemBean.setUuid(saleItem.getItemUuid());
			transactionItemBean.setItem(itemBean);
			transactionItemBean.setCatalogueUuid(saleItem.getCatalogueUuid());
			transactionItemBean.setSubscriptionEndDate(saleItem.getSubscriptionEndDate());
			transactionItemBean.setSubscriptionStartDate(saleItem.getSubscriptionStartDate());
			transactionItemBean.setQuantity(saleItem.getQuantity());
			transactionItemBean.setPrice(convertPrice(saleItem.getPrice(), saleItem.getTax(), currency));
			final String taxCode = saleItem.getTaxCode();
			final List<StoreTaxBean> taxes = Lists.newArrayList();
			if( taxCode != null )
			{
				final StoreTaxBean tax = new StoreTaxBean();
				tax.setCode(taxCode);
				taxes.add(tax);
			}
			transactionItemBean.getPrice().setTaxes(taxes);

			// SaleItem currency can only be that of the Sale
			final String pricingTierUuid = saleItem.getPricingTierUuid();

			if( pricingTierUuid == null )
			{
				transactionItemBean.setFree(true);
			}
			else
			{
				final PricingTier pricingTier = tierService.getByUuid(pricingTierUuid);
				if( pricingTier != null )
				{
					if( pricingTier.isPurchase() )
					{
						transactionItemBean.setPurchaseTier(convertPurchaseTierToBean(pricingTier, purchasePerUser));
					}
					else
					{
						transactionItemBean.setSubscriptionTier(convertSubscriptionTierToBean(pricingTier, subPerUser));
					}
				}
			}
			// a linkToSelf link along the lines of a getCatalogueItemSelfLink
			// would be neat, but how to get a catalogueId?

			transactionItemBeans.add(transactionItemBean);
		}
		transactionBean.setItems(transactionItemBeans);

		return transactionBean;
	}

	private StorePriceBean convertPrice(long value, long tax, Currency currency)
	{
		final StorePriceBean price = convertPriceBase(value, currency);
		price.setTaxValue(new DecimalNumberBean(tax, getCurrencyDecimals(currency)));
		return price;
	}

	private StorePriceBean convertPrice(long value, BigDecimal tax, Currency currency)
	{
		final StorePriceBean price = convertPriceBase(value, currency);
		price.setTaxValue(new DecimalNumberBean(tax));
		return price;
	}

	/**
	 * Don't use
	 * 
	 * @param value
	 * @param currency
	 * @return
	 */
	private StorePriceBean convertPriceBase(long value, Currency currency)
	{
		final StorePriceBean price = new StorePriceBean();
		price.setValue(new DecimalNumberBean(value, getCurrencyDecimals(currency)));
		price.setCurrency(currency == null ? null : currency.getCurrencyCode());
		return price;
	}

	private long getCurrencyDecimals(Currency currency)
	{
		if( currency != null )
		{
			return currency.getDefaultFractionDigits();
		}
		else
		{
			return 0;
		}
	}

	@Override
	public StoreHarvestableItemBean convertItemToHarvestableItemBean(Item item)
	{
		final ItemSerializerItemBean itemBeanSerializer = itemSerializerService.createItemBeanSerializer(
			Collections.singleton(item.getId()), Sets.newHashSet(ItemSerializerService.CATEGORY_ALL), true);

		final StoreHarvestableItemBean itemBean = new StoreHarvestableItemBean();
		itemBean.setUuid(item.getUuid());
		itemBean.setVersion(item.getVersion());
		itemBeanSerializer.writeItemBeanResult(itemBean, item.getId());

		// We need to filter out un-harvestable attachments
		final List<AttachmentBean> actualAttachments = Lists.newArrayList();
		final List<AttachmentBean> origAttachments = itemBean.getAttachments();
		for( AttachmentBean attachment : origAttachments )
		{
			if( attachmentSerializer.exportable((EquellaAttachmentBean) attachment) )
			{
				actualAttachments.add(attachment);
			}
		}
		itemBean.setAttachments(actualAttachments);
		origAttachments.removeAll(actualAttachments);

		// remove NavigationTabs referring to these attachments
		if( !origAttachments.isEmpty() )
		{
			final Set<String> removedAttachments = Sets.newHashSet();
			for( AttachmentBean rem : origAttachments )
			{
				removedAttachments.add(rem.getUuid());
			}

			final NavigationTreeBean navigation = itemBean.getNavigation();
			if( navigation != null )
			{
				final List<NavigationNodeBean> nodes = navigation.getNodes();
				if( !Check.isEmpty(nodes) )
				{
					final Iterator<NavigationNodeBean> nodeIt = nodes.iterator();
					while( nodeIt.hasNext() )
					{
						sanitiseNode(nodeIt, removedAttachments);
					}
				}
			}
		}

		return itemBean;
	}

	private void sanitiseNode(Iterator<NavigationNodeBean> nodeIt, Set<String> removedAttachments)
	{
		final NavigationNodeBean node = nodeIt.next();

		final List<NavigationTabBean> tabs = node.getTabs();
		if( tabs != null )
		{
			final Iterator<NavigationTabBean> tabIt = tabs.iterator();
			while( tabIt.hasNext() )
			{
				NavigationTabBean tab = tabIt.next();
				if( removedAttachments.contains(tab.getAttachment().getUuid()) )
				{
					tabIt.remove();
				}
			}
		}

		final List<NavigationNodeBean> nodes = node.getNodes();
		if( nodes != null )
		{
			final Iterator<NavigationNodeBean> nodeIt2 = nodes.iterator();
			while( nodeIt2.hasNext() )
			{
				sanitiseNode(nodeIt2, removedAttachments);
			}
		}

		if( Check.isEmpty(node.getNodes()) && Check.isEmpty(node.getTabs()) )
		{
			nodeIt.remove();
		}
	}

	@Override
	public StoreCatalogueItemBean convertItemBeanToCatalogueItemBean(EquellaItemBean equellaItemBean,
		String catalogueUuid, boolean free, PricingTier purchaseTier, PricingTier subscriptionTier,
		Function<Price, BigDecimal> taxCalculator, List<TaxType> taxes)
	{
		final StoreCatalogueItemBean storeItemBean = new StoreCatalogueItemBean();
		storeItemBean.setUuid(equellaItemBean.getUuid());
		storeItemBean.setVersion(equellaItemBean.getVersion());
		storeItemBean.setName(new SimpleI18NString(equellaItemBean.getName(), equellaItemBean.getUuid()));
		storeItemBean.setDescription(equellaItemBean.getDescription());
		storeItemBean.setModifiedDate(equellaItemBean.getModifiedDate());
		storeItemBean.setNavigation(equellaItemBean.getNavigation());

		final List<StoreCatalogueAttachmentBean> storeAttachments = Lists.newArrayList();
		for( AttachmentBean attachment : equellaItemBean.getAttachments() )
		{
			if( attachmentSerializer.exportable((EquellaAttachmentBean) attachment) )
			{
				final StoreCatalogueAttachmentBean storeAttachmentBean = new StoreCatalogueAttachmentBean();
				storeAttachmentBean.setUuid(attachment.getUuid());
				storeAttachmentBean.setDescription(attachment.getDescription());
				storeAttachmentBean.setPreview(attachment.isPreview());
				// storeAttachment.setFilename(attachment.getUrl());
				if( attachment instanceof AbstractFileAttachmentBean )
				{
					storeAttachmentBean.setFilename(((AbstractFileAttachmentBean) attachment).getFilename());
				}
				if( attachment instanceof FileAttachmentBean )
				{
					storeAttachmentBean.setParentZip(((FileAttachmentBean) attachment).getParentZip());
				}
				storeAttachmentBean.setAttachmentType(attachment.getRawAttachmentType());

				storeAttachments.add(storeAttachmentBean);
			}
		}
		storeItemBean.setAttachments(storeAttachments);
		final PaymentSettings paymentSettings = configService.getProperties(new PaymentSettings());
		storeItemBean.setFree(free);
		storeItemBean.setPurchaseTier(convertPurchaseTierToBean(purchaseTier, !paymentSettings.isPurchaseFlatRate(),
			taxCalculator, taxes));
		storeItemBean.setSubscriptionTier(convertSubscriptionTierToBean(subscriptionTier,
			!paymentSettings.isSubscriptionFlatRate(), taxCalculator, taxes));

		return storeItemBean;
	}

	/**
	 * Populate name from saleItem's item, key dates from saleItem's Sale
	 */
	@Override
	public StoreHarvestableItemBean convertSaleItemToStoreHarvestableItemBean(SaleItem saleItem)
	{
		final String itemUuid = saleItem.getItemUuid();
		final int liveVersion = itemService.getLiveItemVersion(itemUuid);
		final Item item = itemService.getUnsecure(new ItemId(itemUuid, liveVersion));

		StoreHarvestableItemBean itemBean = convertItemToHarvestableItemBean(item);
		// Um... why?
		// harvestableSalesInfoBean.setCreatedDate(saleItem.getSale().getCreationDate());
		// harvestableSalesInfoBean.setModifiedDate(saleItem.getSale().getPaidDate());

		// More likely than not be nulls for these values, but go through the
		// motions
		itemBean.setSubscriptionStartDate(saleItem.getSubscriptionStartDate());
		itemBean.setSubscriptionEndDate(saleItem.getSubscriptionEndDate());

		return itemBean;
	}

	@Override
	protected AbstractEntityService<?, BaseEntity> getEntityService()
	{
		return null;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.STORE;
	}
}
