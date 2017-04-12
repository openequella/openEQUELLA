package com.tle.core.payment.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.Pair;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.Sale.PaidStatus;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.SaleSearchResults;
import com.tle.core.payment.dao.SaleDao;
import com.tle.core.payment.dao.SaleItemDao;
import com.tle.core.payment.operation.OperationFactory;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.CatalogueService.CatalogueInfo;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.SaleService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(SaleService.class)
@Singleton
public class SaleServiceImpl implements SaleService
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(SaleServiceImpl.class);

	@Inject
	private SaleDao saleDao;
	@Inject
	private SaleItemDao saleItemDao;
	@Inject
	private ConfigurationService configService;
	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catService;
	@Inject
	private ItemService itemService;
	@Inject
	private PaymentService paymentService;
	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private OperationFactory operationFactory;

	@Transactional
	@Override
	public void checkout(StoreFront storefront, Sale sale)
	{
		final Sale existingSale = saleDao.getByCustomerRef(sale.getCustomerReference());
		if( existingSale != null )
		{
			final PaidStatus paidStatus = existingSale.getPaidStatus();
			if( paidStatus == PaidStatus.PAID )
			{
				throw invalid("customerReference", "paidalready", existingSale.getPaidDate());
			}
			else if( paidStatus == PaidStatus.PENDING )
			{
				throw invalid("customerReference", "pendingalready", existingSale.getPaidDate());
			}
			else
			{
				// The last sale wasn't paid for and we are trying again
				saleDao.delete(existingSale);
			}
		}

		BigDecimal calcPrice = BigDecimal.ZERO;
		BigDecimal calcTax = BigDecimal.ZERO;

		final PaymentSettings paymentSettings = configService.getProperties(new PaymentSettings());
		final TaxType tax = storefront.getTaxType();
		validateSale(sale, paymentSettings, tax);

		final List<SaleItem> saleItems = sale.getSales();
		int index = 0;
		for( SaleItem saleItem : saleItems )
		{
			final Pair<BigDecimal, BigDecimal> calcItemPrice = validateSaleItem(storefront, sale, paymentSettings, tax,
				saleItem, index);
			calcPrice = calcPrice.add(calcItemPrice.getFirst());
			calcTax = calcTax.add(calcItemPrice.getSecond());
			index++;
		}

		final Currency currency = sale.getCurrency();
		final int digits = (currency == null ? 0 : currency.getDefaultFractionDigits());
		if( calcPrice.movePointRight(digits).longValue() != sale.getPrice() )
		{
			throw invalid("price", "pricemismatch", sale.getPrice(), calcPrice);
		}
		if( calcTax.movePointRight(digits).longValue() != sale.getTax() )
		{
			throw invalid("tax", "taxpricemismatch", sale.getTax(), calcTax);
		}

		sale.setInstitution(CurrentInstitution.get());
		sale.setCreationDate(new Date());
		sale.setStorefront(storefront);
		sale.setUuid(UUID.randomUUID().toString());
		for( SaleItem saleItem : saleItems )
		{
			saleItem.setUuid(UUID.randomUUID().toString());
			saleItem.setSale(sale);
		}

		// if total is zero, do an instant payment
		if( calcPrice.compareTo(BigDecimal.ZERO) == 0 )
		{
			commit(storefront, sale, null);
		}
		else
		{
			sale.setPaidStatus(PaidStatus.NONE);
			sale.setPaidDate(null);
			saleDao.save(sale);
		}
	}

	private void validateSale(Sale sale, PaymentSettings paymentSettings, TaxType tax)
	{
		final Set<String> itemUuids = Sets.newHashSet();

		if( sale.getUuid() != null )
		{
			throw invalid("uuid", "uuidassigned");
		}

		final List<SaleItem> saleItems = sale.getSales();
		if( saleItems == null || saleItems.size() == 0 )
		{
			throw invalid("items", "noitems");
		}

		for( SaleItem saleItem : saleItems )
		{
			final String itemUuid = saleItem.getItemUuid();

			// an item is listed in the checkout more than once
			if( itemUuids.contains(itemUuid) )
			{
				throw invalid("items", "itemduplicate");
			}
			itemUuids.add(itemUuid);
		}

		// check if tax supplied, but none applicable
		final String taxCode = sale.getTaxCode();
		final BigDecimal taxPercent = sale.getTaxPercent();
		if( tax == null )
		{
			if( taxCode != null )
			{
				throw invalid("taxCode", "unexpectedtaxcode", taxCode);
			}
			if( taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) != 0 )
			{
				throw invalid("taxPercent", "unexpectedtaxpercent", taxPercent.doubleValue());
			}
		}
		else
		{
			// Ensure applicable taxes supplied (if total > 0)
			if( sale.getPrice() != 0 )
			{
				if( taxCode == null )
				{
					throw invalid("taxCode", "missingtaxcode", tax.getCode());
				}
				else if( !taxCode.equals(tax.getCode()) )
				{
					throw invalid("taxCode", "invalidtaxcode", taxCode, tax.getCode());
				}

				if( taxPercent == null )
				{
					throw invalid("taxPercent", "missingtaxpercent", tax.getPercent().doubleValue());
				}
				else if( taxPercent.compareTo(tax.getPercent()) != 0 )
				{
					throw invalid("taxPercent", "invalidtaxpercent", taxPercent.doubleValue(), tax.getPercent()
						.doubleValue());
				}
			}
		}

		if( sale.getPrice() != 0 )
		{
			final Currency currency = sale.getCurrency();
			if( currency == null )
			{
				throw invalid("currency", "nocurrencywithtotal");
			}

			// Remove when more than one currency enabled
			if( !currency.equals(Currency.getInstance(paymentSettings.getCurrency())) )
			{
				throw invalid("currency", "mismatchcurrency");
			}
		}
		// TODO: Um...? Why do we need this?
		else
		{
			sale.setCurrency(Currency.getInstance(paymentSettings.getCurrency()));
		}
	}

	/**
	 * @param field
	 * @param key Will be prefixed by "sale.validation."
	 * @return
	 */
	private InvalidDataException invalid(String field, String key, Object... params)
	{
		return new InvalidDataException(new ValidationError(field,
			resources.getString("sale.validation." + key, params)));
	}

	/**
	 * @param storefront
	 * @param saleItem
	 * @return The calculated (price,tax) pair of the item
	 */
	private Pair<BigDecimal, BigDecimal> validateSaleItem(StoreFront storefront, Sale sale,
		PaymentSettings paymentSettings, TaxType tax, SaleItem saleItem, int index)
	{
		final String uuid = saleItem.getItemUuid();
		if( uuid == null )
		{
			throw invalid("items[" + index + "].uuid", "saleitem.itemuuid");
		}

		final ItemId itemKey = new ItemId(uuid, itemService.getLiveItemVersion(uuid));

		final PricingTierAssignment tierAss = tierService.getPricingTierAssignmentForItem(itemKey);
		// the item is not even assigned a price
		if( tierAss == null )
		{
			// Does not exist as far as store front is concerned
			throw invalid("items[" + index + "].uuid", "saleitem.itemnotfound");
		}

		// an item is not available in any catalogue, so the store front
		// should never have been able to see it
		final CatalogueInfo catInfo = catService.groupCataloguesForItem(itemService.getUnsecure(itemKey));
		final List<Catalogue> assignedCatalogues = catInfo.getDynamic();
		final List<Catalogue> allowedCatalogues = catService.enumerateForCountry(storefront.getCountry());
		assignedCatalogues.retainAll(allowedCatalogues);
		if( assignedCatalogues.size() == 0 )
		{
			throw invalid("items[" + index + "].uuid", "saleitem.itemnotincatalogue");
		}

		final String requestedTierUuid = saleItem.getPricingTierUuid();
		final boolean requestedFree = (requestedTierUuid == null);

		// a tier representation is missing from CheckoutItem that the store
		// front is not allowed to get for free.
		if( requestedFree && !canBeFree(storefront, paymentSettings, tierAss) )
		{
			throw invalid("items[" + index + "].purchaseTier", "saleitem.itemnotfree");
		}

		// free has checked out OK
		if( requestedFree )
		{
			return new Pair<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO);
		}

		if( requestedTierUuid == null )
		{
			throw invalid("items[" + index + "].purchaseTier", "saleitem.notier");
		}

		// a tier representation points to a tier that is not assigned to
		// that item.
		final PricingTier resolvedTier = canBeTier(storefront, paymentSettings, tierAss,
			tierService.getByUuid(requestedTierUuid));
		if( resolvedTier == null )
		{
			throw invalid("items[" + index + "].purchaseTier", "saleitem.tiernotallowed");
		}

		final int decimals = (sale.getCurrency() == null ? 0 : sale.getCurrency().getDefaultFractionDigits());

		final BigDecimal unitPrice;
		final boolean flatRate;

		if( resolvedTier.isPurchase() )
		{
			final Price price = tierService.getPriceForPurchaseTier(resolvedTier);
			if( !price.getCurrency().equals(sale.getCurrency()) )
			{
				throw invalid("items[" + index + "].currency", "saleitem.invalidcurrency");
			}

			unitPrice = new BigDecimal(price.getValue()).movePointLeft(decimals);
			flatRate = paymentSettings.isPurchaseFlatRate();
		}
		else
		{
			final SubscriptionPeriod period = saleItem.getPeriod();
			if( period == null )
			{
				throw invalid("items[" + index + "].subscriptionPeriod", "saleitem.missingsubscriptionperiod");
			}
			final Price price = tierService.getPriceForSubscriptionTierAndPeriod(resolvedTier, period);
			if( !price.getCurrency().equals(sale.getCurrency()) )
			{
				throw invalid("items[" + index + "].currency", "saleitem.invalidcurrency");
			}

			unitPrice = new BigDecimal(price.getValue()).movePointLeft(decimals);
			flatRate = paymentSettings.isSubscriptionFlatRate();
		}

		final BigDecimal taxPercent = (tax == null ? BigDecimal.ZERO : tax.getPercent().movePointLeft(2));
		// Important! tax applies to the unit price and is rounded
		// accordingly at that level!
		final BigDecimal unitTax = unitPrice.multiply(taxPercent).setScale(decimals, RoundingMode.HALF_UP);

		final BigDecimal total;
		final BigDecimal taxTotal;
		final int quantity = saleItem.getQuantity();
		if( !flatRate )
		{
			// a quantity of zero is specified for a per-user tier
			if( quantity == 0 )
			{
				throw invalid("items[" + index + "].quantity", "saleitem.missingqty");
			}
			total = unitPrice.multiply(new BigDecimal(quantity));
			taxTotal = unitTax.multiply(new BigDecimal(quantity));
		}
		else
		{
			// a quantity of non-zero is specified for a fixed rate tier
			if( quantity != 0 )
			{
				throw invalid("items[" + index + "].quantity", "saleitem.notqty");
			}
			total = unitPrice;
			taxTotal = unitTax;
		}

		// validate price and tax against supplied
		final BigDecimal suppliedTotal = new BigDecimal(saleItem.getPrice()).movePointLeft(decimals);
		final BigDecimal suppliedTaxTotal = new BigDecimal(saleItem.getTax()).movePointLeft(decimals);
		if( suppliedTotal.compareTo(total) != 0 )
		{
			throw invalid("items[" + index + "].price.value", "saleitem.pricemismatch", suppliedTotal.doubleValue(),
				total.doubleValue());
		}
		if( suppliedTaxTotal.compareTo(taxTotal) != 0 )
		{
			throw invalid("items[" + index + "].price.taxValue", "saleitem.taxpricemismatch",
				suppliedTaxTotal.doubleValue(), taxTotal.doubleValue());
		}

		return new Pair<BigDecimal, BigDecimal>(total, taxTotal);
	}

	private boolean canBeFree(StoreFront storefront, PaymentSettings paymentSettings, PricingTierAssignment tierAss)
	{
		if( !paymentSettings.isFreeEnabled() )
		{
			return false;
		}
		if( !storefront.isAllowFree() )
		{
			return false;
		}
		if( tierAss == null )
		{
			return false;
		}
		return tierAss.isFreeItem();
	}

	/**
	 * @param storefront
	 * @param paymentSettings
	 * @param tierAss
	 * @param requestedTier
	 * @return null if requested tier is invalid, otherwise the relevant
	 *         assigned tier
	 */
	private PricingTier canBeTier(StoreFront storefront, PaymentSettings paymentSettings,
		PricingTierAssignment tierAss, PricingTier requestedTier)
	{
		final boolean purchase = requestedTier.isPurchase();
		if( purchase && !paymentSettings.isPurchaseEnabled() )
		{
			return null;
		}
		if( purchase && !storefront.isAllowPurchase() )
		{
			return null;
		}
		if( !purchase && !storefront.isAllowSubscription() )
		{
			return null;
		}
		if( tierAss == null )
		{
			return null;
		}

		if( purchase )
		{
			final PricingTier purchasePricingTier = tierAss.getPurchasePricingTier();
			if( purchasePricingTier == null )
			{
				return null;
			}
			if( !requestedTier.getUuid().equals(purchasePricingTier.getUuid()) )
			{
				return null;
			}
			return purchasePricingTier;
		}

		final PricingTier subscriptionPricingTier = tierAss.getSubscriptionPricingTier();
		if( subscriptionPricingTier == null )
		{
			return null;
		}
		if( !requestedTier.getUuid().equals(subscriptionPricingTier.getUuid()) )
		{
			return null;
		}
		return subscriptionPricingTier;
	}

	@Transactional
	@Override
	public void commit(StoreFront storefront, Sale sale, String receipt)
	{
		sale.setPaidStatus(PaidStatus.PAID);
		sale.setPaidDate(new Date());
		sale.setReceipt(receipt);

		final Date now = DateUtils.truncate(new Date(), Calendar.DATE);
		for( SaleItem sitem : sale.getSales() )
		{
			if( sitem.getPeriod() != null && sitem.getSubscriptionStartDate() == null )
			{
				sitem.setSubscriptionStartDate(now);
				sitem.setSubscriptionEndDate(paymentService.getEndDateOfSubscriptionPeriod(now, sitem.getPeriod()));
			}
		}

		saleDao.save(sale);
		List<SaleItem> saleItems = sale.getSales();
		for( SaleItem saleItem : saleItems )
		{
			int itemVersion = saleItem.getItemVersion();
			if( itemVersion == 0 )
			{
				itemVersion = itemService.getLiveItemVersion(saleItem.getItemUuid());
			}

			itemService.operation(new ItemId(saleItem.getItemUuid(), itemVersion), operationFactory.itemSold(),
				workflowFactory.reIndexIfRequired());
		}

	}

	@Transactional
	@Override
	public void setPending(StoreFront storefront, Sale sale)
	{
		sale.setPaidStatus(PaidStatus.PENDING);
		saleDao.save(sale);
	}

	@Override
	public Sale getSale(StoreFront storefront, String saleUuid)
	{
		Sale sale = saleDao.get(saleUuid);
		// verify storefront
		if( storefront != null )
		{
			if( !sale.getStorefront().equals(storefront) )
			{
				// Embellish the logged error message if it appears that the
				// storefront
				// and the sale have a common institution, but nonetheless
				// different storefronts (may help support unentangle, should it
				// ever be seen in the wild.
				Institution saleInsti = sale.getInstitution();
				Institution storefrontInsti = storefront.getInstitution();
				if( saleInsti.equals(storefrontInsti) )
				{
					throw new RuntimeException("Invalid storefront making call for saleUuid, same institution ("
						+ saleInsti.getName() + ") apparently has multiple but distinct storefronts");
				}
				else
				// some other problem
				{
					throw new RuntimeException("Invalid storefront making call for saleUuid (id:" + storefront.getId()
						+ ", institution: " + storefrontInsti.getName() + "), saleUuid: " + saleUuid);
				}
			}
		}
		return sale;
	}

	@Override
	public Sale getByReceipt(String receipt)
	{
		return saleDao.getByReceipt(receipt);
	}

	@Override
	public List<Sale> enumerateForStoreFront(StoreFront storefront)
	{
		return saleDao.enumerateForStoreFront(storefront);
	}

	@Override
	public SaleSearchResults search(StoreFront storefront, int offset, int count, String customerReference)
	{
		return saleDao.search(storefront, offset, count, customerReference);
	}

	@Override
	public List<SaleItem> getSalesItemsForSourceItem(String itemUuid)
	{
		return saleItemDao.getSalesItemsForSourceItem(itemUuid);
	}
}
