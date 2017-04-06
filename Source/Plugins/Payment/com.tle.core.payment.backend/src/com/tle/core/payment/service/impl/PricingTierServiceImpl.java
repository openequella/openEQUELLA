package com.tle.core.payment.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.events.ItemDeletedEvent;
import com.tle.core.events.listeners.ItemDeletedListener;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.PaymentIndexFields;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.dao.CatalogueDao;
import com.tle.core.payment.dao.PriceDao;
import com.tle.core.payment.dao.PricingTierAssignmentDao;
import com.tle.core.payment.dao.PricingTierDao;
import com.tle.core.payment.dao.RegionDao;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.core.payment.events.PricingTierDeletionEvent;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.session.PricingTierEditingBean;
import com.tle.core.payment.service.session.PricingTierEditingBean.PriceBean;
import com.tle.core.payment.service.session.PricingTierEditingSession;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;
import com.tle.core.services.item.ItemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
@Bind(PricingTierService.class)
@Singleton
@SecureEntity(PricingTier.ENTITY_TYPE)
public class PricingTierServiceImpl
	extends
		AbstractEntityServiceImpl<PricingTierEditingBean, PricingTier, PricingTierService>
	implements
		PricingTierService,
		ItemDeletedListener
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(PricingTierServiceImpl.class);
	private static final String KEY_VALIDATION_PRICE_NONNEG = "validation.price.nonnegative";
	private static final String KEY_VALIDATION_PRICE_POSITIVE = "validation.price.positive";
	private static final String KEY_VALIDATION_PRICE_TOOBIG = "validation.price.toobig";
	private static final String KEY_VALIDATION_PRICE_TRUNCATE = "validation.price.truncate";
	private static final String KEY_VALIDATION_PRICE_EMPTY = "validation.price.empty";

	private static final String KEY_VALIDATION_PRICES_ATLEASTONEEENABLED = "validation.prices.atleastoneenabled";
	private static final String KEY_NOT_SET_TIER = "error.set.tier";
	private static final String KEY_NO_TIERS = "error.no.tiers";

	// A bit arbitrary
	private static final double MAX_PRICE = 1000000000.0;

	private final PricingTierDao tierDao;

	@Inject
	private PriceDao priceDao;
	@Inject
	private SubscriptionPeriodDao spDao;
	@Inject
	private RegionDao regionDao;
	@Inject
	private CatalogueDao catalogueDao;
	@Inject
	private PricingTierAssignmentDao ptaDao;
	@Inject
	private ItemService itemService;
	@Inject
	private ConfigurationService configService;

	@Inject
	public PricingTierServiceImpl(PricingTierDao tierDao)
	{
		super(Node.TIER, tierDao);
		this.tierDao = tierDao;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected PricingTierEditingBean createEditingBean()
	{
		return new PricingTierEditingBean();
	}

	@Override
	protected void cleanCloneBeans(PricingTierEditingBean bean)
	{
		super.cleanCloneBeans(bean);

		final PricingTierEditingBean ptBean = bean;
		final PriceBean pp = ptBean.getPurchasePrice();
		if( pp != null )
		{
			pp.setId(0);
		}

		final List<PriceBean> subscriptionPrices = ptBean.getSubscriptionPrices();
		if( subscriptionPrices != null )
		{
			for( PriceBean pb : subscriptionPrices )
			{
				pb.setId(0);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<PricingTierEditingBean, PricingTier>> SESSION createSession(
		String sessionId, EntityPack<PricingTier> pack, PricingTierEditingBean bean)
	{
		return (SESSION) new PricingTierEditingSession(sessionId, pack, bean);
	}

	@Override
	protected void doValidation(EntityEditingSession<PricingTierEditingBean, PricingTier> session, PricingTier tier,
		List<ValidationError> errors)
	{
		// Validation on editing bean only
	}

	@Override
	protected void doValidationBean(PricingTierEditingBean tierBean, List<ValidationError> errors)
	{
		super.doValidationBean(tierBean, errors);

		if( tierBean.isPurchase() )
		{
			validatePrice(tierBean.getPurchasePrice(), true, errors);
		}
		else
		{
			final List<PriceBean> priceList = tierBean.getSubscriptionPrices();
			for( PriceBean pb : priceList )
			{
				if( pb.isEnabled() )
				{
					validatePrice(pb, false, errors);
				}
			}
		}

		if( !tierBean.isPurchase() )
		{
			boolean anyEnabled = false;
			final List<PriceBean> prices = tierBean.getSubscriptionPrices();
			for( PriceBean pb : prices )
			{
				if( pb.isEnabled() )
				{
					anyEnabled = true;
					break;
				}
			}

			if( !anyEnabled )
			{
				errors.add(new ValidationError("price.subscription.general", resources
					.getString(KEY_VALIDATION_PRICES_ATLEASTONEEENABLED)));
			}
		}
	}

	private void validatePrice(PriceBean pb, boolean purchase, List<ValidationError> errors)
	{
		try
		{
			final BigDecimal num = parseNumber(pb, purchase, errors);
			if( num == null )
			{
				return;
			}

			final int compareToZero = num.compareTo(BigDecimal.ZERO);
			if( purchase && compareToZero <= 0 )
			{
				errors.add(nonZeroError(pb));
			}
			else if( !purchase && compareToZero < 0 )
			{
				errors.add(nonNegError(pb));
			}

			if( num.compareTo(new BigDecimal(MAX_PRICE)) > 0 )
			{
				errors.add(tooBigError(pb, purchase));
			}
		}
		catch( ParseException p )
		{
			errors.add(purchase ? nonZeroError(pb) : nonNegError(pb));
		}
	}

	/**
	 * Normal Java parsing is evil. It doesn't blow up for strings such as
	 * "23.45hhhjd##!", instead it returns 23.45
	 * 
	 * @param stringVal
	 * @return
	 */
	private BigDecimal parseNumber(PriceBean pb, boolean purchase, List<ValidationError> errors) throws ParseException
	{
		final String value = pb.getValue();
		if( Check.isEmpty(value) )
		{
			errors.add(new ValidationError(getPriceErrorKey(pb, purchase), resources
				.getString(KEY_VALIDATION_PRICE_EMPTY)));
			return null;
		}

		final Currency currency = Currency.getInstance(pb.getCurrency());

		// Using current locale, which is not good. It would be better if it
		// were possible to reverse lookup the locale based on the currency
		final DecimalFormat df = (DecimalFormat) (NumberFormat.getNumberInstance(CurrentLocale.getLocale()));
		final int defaultFractionDigits = currency.getDefaultFractionDigits();
		if( defaultFractionDigits != -1 )
		{
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(defaultFractionDigits);
		}

		// See
		// http://stackoverflow.com/questions/10906522/how-to-convert-formatted-strings-to-float
		//
		// Have to use the ParsePosition API or else it will silently stop
		// parsing even though some of the characters weren't part of the parsed
		// number.
		final ParsePosition position = new ParsePosition(0);
		df.setParseBigDecimal(true);
		final BigDecimal parsed = (BigDecimal) df.parseObject(value, position);

		// getErrorIndex() doesn't seem to accurately reflect errors, but
		// getIndex() does reflect how far we successfully parsed.
		if( position.getIndex() == value.length() )
		{
			if( parsed.scale() > defaultFractionDigits )
			{
				errors.add(new ValidationError(getPriceErrorKey(pb, purchase), resources
					.getString(KEY_VALIDATION_PRICE_TRUNCATE)));
				return null;
			}
			return parsed;
		}
		throw new ParseException(value, position.getIndex());
	}

	private ValidationError nonZeroError(PriceBean pb)
	{
		return new ValidationError(getPriceErrorKey(pb, true), resources.getString(KEY_VALIDATION_PRICE_POSITIVE));
	}

	private ValidationError nonNegError(PriceBean pb)
	{
		return new ValidationError(getPriceErrorKey(pb, false), resources.getString(KEY_VALIDATION_PRICE_NONNEG));
	}

	private ValidationError tooBigError(PriceBean pb, boolean purchase)
	{
		return new ValidationError(getPriceErrorKey(pb, purchase), resources.getString(KEY_VALIDATION_PRICE_TOOBIG));
	}

	private String getPriceErrorKey(PriceBean pb, boolean purchase)
	{
		if( purchase )
		{
			return "price.purchase.value";
		}
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("price.subscription.");
		stringBuilder.append(pb.getSubscriptionPeriodId());
		stringBuilder.append(".value");
		return stringBuilder.toString();
	}

	@Override
	protected void populateEditingBean(PricingTierEditingBean ptBean, PricingTier tier)
	{
		super.populateEditingBean(ptBean, tier);
		ptBean.setPurchase(tier.isPurchase());

		// mirror of this part is doAfterImport
		if( ptBean.getId() != 0 )
		{
			if( ptBean.isPurchase() )
			{
				final Price p = priceDao.getForPurchaseTier(tier);
				ptBean.setPurchasePrice((p == null ? null : convertPrice(p)));
			}
			else
			{
				final List<PriceBean> priceList = ptBean.getSubscriptionPrices();
				priceList.clear();
				for( Price p : priceDao.enumerateAllForSubscriptionTier(tier) )
				{
					priceList.add(convertPrice(p));
				}
			}
		}
	}

	@Override
	protected void populateEntity(PricingTierEditingBean ptBean, PricingTier tier)
	{
		super.populateEntity(ptBean, tier);
		tier.setPurchase(ptBean.isPurchase());
	}

	@Override
	protected void doAfterImport(TemporaryFileHandle importFolder, PricingTierEditingBean ptBean, PricingTier tier,
		ConverterParams params)
	{
		super.doAfterImport(importFolder, ptBean, tier, params);

		if( ptBean != null )
		{
			if( tier.isPurchase() )
			{
				PriceBean pb = ptBean.getPurchasePrice();

				final Price dbPrice = pb.getId() != 0 ? priceDao.findById(pb.getId()) : new Price();
				Price price = convertPriceBean(tier, pb, dbPrice);
				priceDao.saveOrUpdate(price);
			}
			else
			{
				final List<PriceBean> priceList = ptBean.getSubscriptionPrices();
				final Map<Long, Price> priceMap = priceDao.findByIds(Lists.transform(priceList,
					new Function<PriceBean, Long>()
					{
						@Override
						public Long apply(PriceBean priceBean)
						{
							return priceBean.getId();
						}
					}));
				for( PriceBean pb : priceList )
				{
					if( pb.isEnabled() )
					{
						final Price dbPrice = (pb.getId() != 0 ? priceMap.get(pb.getId()) : new Price());
						Price price = convertPriceBean(tier, pb, dbPrice);
						priceDao.saveOrUpdate(price);
					}
					else
					{
						final Price dbPrice = (pb.getId() != 0 ? priceMap.get(pb.getId()) : null);
						if( dbPrice != null )
						{
							priceDao.delete(dbPrice);
						}
					}
				}
			}
		}
	}

	private PriceBean convertPrice(Price price)
	{
		final Currency currency = price.getCurrency();

		final PriceBean pb = new PriceBean();
		pb.setId(price.getId());
		pb.setCatalogueId(price.getCatalogue() == null ? 0 : price.getCatalogue().getId());
		pb.setCurrency(currency.getCurrencyCode());
		pb.setEnabled(price.isEnabled());
		pb.setRegionId(price.getRegion() == null ? 0 : price.getRegion().getId());
		pb.setSubscriptionPeriodId(price.getPeriod() == null ? 0 : price.getPeriod().getId());

		final NumberFormat df = NumberFormat.getNumberInstance(CurrentLocale.getLocale());
		if( currency.getDefaultFractionDigits() != -1 )
		{
			df.setMinimumFractionDigits(currency.getDefaultFractionDigits());
		}
		pb.setValue(df.format(price.getDoubleValue()));

		return pb;
	}

	private Price convertPriceBean(PricingTier tier, PriceBean pb, Price price)
	{
		final Currency currency = Currency.getInstance(pb.getCurrency());
		price.setTier(tier);
		price.setCurrency(currency);
		price.setEnabled(pb.isEnabled());

		long periodId = pb.getSubscriptionPeriodId();
		if( periodId != 0 && (price.getPeriod() == null || price.getPeriod().getId() != periodId) )
		{
			price.setPeriod(spDao.findById(periodId));
		}
		long catId = pb.getCatalogueId();
		if( catId != 0 && (price.getCatalogue() == null || price.getCatalogue().getId() != catId) )
		{
			price.setCatalogue(catalogueDao.findById(catId));
		}
		long regionId = pb.getRegionId();
		if( regionId != 0 && (price.getRegion() == null || price.getRegion().getId() != regionId) )
		{
			price.setRegion(regionDao.findById(regionId));
		}

		final List<ValidationError> errors = Lists.newArrayList();
		try
		{
			final BigDecimal num = parseNumber(pb, tier.isPurchase(), errors);
			final BigDecimal noDecimal = num.movePointRight(currency.getDefaultFractionDigits());
			price.setValue(noDecimal.longValue());
		}
		catch( ParseException pe )
		{
			throw Throwables.propagate(pe);
		}
		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}

		return price;
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public List<BaseEntityLabel> listEditable(boolean purchase)
	{
		return listAll(purchase);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public List<PricingTier> enumerateEditable(boolean purchase)
	{
		return tierDao.enumerateAll(purchase);
	}

	@Override
	public List<BaseEntityLabel> listAll(boolean purchase)
	{
		return tierDao.listAll(purchase);
	}

	@Override
	public List<BaseEntityLabel> listEnabled(boolean purchase)
	{
		return tierDao.listEnabled(purchase);
	}

	@Override
	public List<PricingTier> enumerateAll(boolean purchase)
	{
		return tierDao.enumerateAll(purchase);
	}

	@Override
	public List<PricingTier> enumerateEnabled(boolean purchase)
	{
		return tierDao.enumerateEnabled(purchase);
	}

	@Override
	public List<PricingTierAssignment> listAssignmentsForTier(PricingTier tier)
	{
		return ptaDao.enumerateForTier(tier);
	}

	@Override
	public Map<Item, PricingTierAssignment> getAssignmentsForItems(List<Item> items)
	{
		return ptaDao.getAssignmentsForItems(items);
	}

	@Override
	@Transactional
	public Long createPricingTierAssignment(ItemKey itemKey, PricingTier purchaseTier, PricingTier subscriptionTier,
		boolean free)
	{
		Item item = itemService.getUnsecure(itemKey);
		ensureEdit(item);
		PricingTierAssignment assignment = new PricingTierAssignment();
		assignment.setItem(item);
		assignment.setPurchasePricingTier(purchaseTier);
		assignment.setSubscriptionPricingTier(subscriptionTier);
		assignment.setFreeItem(free);

		Long assignmentId = ptaDao.save(assignment);

		return assignmentId;
	}

	private void ensureEdit(Item item)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(PaymentConstants.PRIV_SET_TIERS_FOR_ITEM);
		if( aclManager.filterNonGrantedPrivileges(item, privs).isEmpty() )
		{
			throw new AccessDeniedException(resources.getString(KEY_NOT_SET_TIER));
		}
	}

	@Override
	@Transactional
	public PricingTierAssignment getPricingTierAssignmentForItem(ItemKey itemKey)
	{
		return ptaDao.getForItem(itemService.getUnsecure(itemKey));
	}

	@Override
	@Transactional
	public Long savePricingTierAssignment(PricingTierAssignment pricingTierAssignment)
	{
		ensureEdit(pricingTierAssignment.getItem());
		return ptaDao.save(pricingTierAssignment);
	}

	@Override
	public Price getPriceForPurchaseTier(PricingTier tier)
	{
		if( !tier.isPurchase() )
		{
			throw new IllegalArgumentException("Tier type must be purchase to use this method");
		}
		return priceDao.getForPurchaseTier(tier);
	}

	@Override
	public Price getPriceForSubscriptionTierAndPeriod(PricingTier tier, SubscriptionPeriod period)
	{
		if( tier.isPurchase() )
		{
			throw new IllegalArgumentException("Tier type must be subscription to use this method");
		}
		return priceDao.getForSubscriptionTierAndPeriod(tier, period);
	}

	@Override
	public Map<SubscriptionPeriod, Price> getPriceMapForSubscriptionTier(PricingTier tier)
	{
		if( tier.isPurchase() )
		{
			throw new IllegalArgumentException("Tier type must be subscription to use this method");
		}
		return priceDao.getPriceMapForSubscriptionTier(tier);
	}

	@Override
	public List<Price> enumeratePricesForSubscriptionTier(PricingTier tier)
	{
		if( tier.isPurchase() )
		{
			throw new IllegalArgumentException("Tier type must be subscription to use this method");
		}
		return priceDao.enumerateAllForSubscriptionTier(tier);
	}

	@Override
	protected void deleteReferences(PricingTier tier)
	{
		super.deleteReferences(tier);
		publishEvent(new PricingTierDeletionEvent(tier));
	}

	// @Override
	// public List<Class<?>> getReferencingClasses(long id)
	// {
	// return tierDao.getReferencingClasses(id);
	// }

	@Override
	@Transactional
	public ListMultimap<PricingTier, Price> getEnabledTierPriceMap(boolean purchase)
	{
		return tierDao.getEnabledTierPriceMap(purchase);
	}

	@Override
	@Transactional
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		// Dodgy, but hey, I copied this temp item hack from HierarchyService
		Item item = new Item();
		item.setId(event.getKey());

		final PricingTierAssignment pta = ptaDao.getForItem(item);
		if( pta != null )
		{
			ptaDao.delete(pta);
		}
	}

	@Override
	public FreeTextQuery getPriceSetQuery(boolean isSet)
	{
		FreeTextBooleanQuery pricingQuery = new FreeTextBooleanQuery(!isSet, false);

		List<BaseEntityLabel> purchaseTiers = listAll(true);
		for( BaseEntityLabel tier : purchaseTiers )
		{
			pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_PURCHASE_TIER, tier.getUuid()));
		}
		List<BaseEntityLabel> subscriptionTiers = listAll(false);
		for( BaseEntityLabel tier : subscriptionTiers )
		{
			pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_SUBSCRIPTION_TIER, tier.getUuid()));
		}
		pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_FREE_TIER, Boolean.toString(true)));

		return pricingQuery;
	}

	@Override
	public FreeTextQuery getPriceSetQuery(boolean isSet, StoreFront storefront)
	{
		final PaymentSettings settings = configService.getProperties(new PaymentSettings());
		FreeTextBooleanQuery pricingQuery = new FreeTextBooleanQuery(!isSet, false);
		boolean hasTiers = false;

		List<BaseEntityLabel> purchaseTiers = listEnabled(true);
		if( settings.isPurchaseEnabled() && storefront.isAllowPurchase() )
		{
			hasTiers = true;
			for( BaseEntityLabel tier : purchaseTiers )
			{
				pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_PURCHASE_TIER, tier.getUuid()));
			}
		}

		List<BaseEntityLabel> subscriptionTiers = listEnabled(false);
		if( settings.isSubscriptionEnabled() && storefront.isAllowSubscription() )
		{
			hasTiers = true;
			for( BaseEntityLabel tier : subscriptionTiers )
			{
				pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_SUBSCRIPTION_TIER, tier.getUuid()));
			}
		}

		if( settings.isFreeEnabled() && storefront.isAllowFree() )
		{
			hasTiers = true;
			pricingQuery.add(new FreeTextFieldQuery(PaymentIndexFields.FIELD_FREE_TIER, Boolean.toString(true)));
		}

		if( !hasTiers )
		{
			throw new RuntimeException(resources.getString(KEY_NO_TIERS));
		}

		return pricingQuery;
	}
}
