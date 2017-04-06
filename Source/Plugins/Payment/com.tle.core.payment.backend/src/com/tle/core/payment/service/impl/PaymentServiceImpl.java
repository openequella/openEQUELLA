package com.tle.core.payment.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.PriceDao;
import com.tle.core.payment.dao.StoreHarvestInfoDao;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.core.payment.events.listeners.CatalogueDeletionListener;
import com.tle.core.payment.events.listeners.CatalogueReferencesListener;
import com.tle.core.payment.events.listeners.PricingTierDeletionListener;
import com.tle.core.payment.events.listeners.RegionDeletionListener;
import com.tle.core.payment.events.listeners.RegionReferencesListener;
import com.tle.core.payment.service.PaymentService;

@Bind(PaymentService.class)
@Singleton
public class PaymentServiceImpl
	implements
		PaymentService,
		RegionDeletionListener,
		RegionReferencesListener,
		PricingTierDeletionListener,
		CatalogueDeletionListener,
		CatalogueReferencesListener

{
	@Inject
	private SubscriptionPeriodDao spDao;
	@Inject
	private PriceDao priceDao;
	@Inject
	private StoreHarvestInfoDao storeHarvestInfoDao;

	private volatile Set<Currency> currencies;
	private final Object currencyLock = new Object();

	@Override
	public List<SubscriptionPeriod> enumerateSubscriptionPeriods()
	{
		return spDao.enumerateAll();
	}

	@Override
	public SubscriptionPeriod getSubscriptionPeriodByUuid(String uuid)
	{
		return spDao.getByUuid(uuid);
	}

	@SuppressWarnings("nls")
	@Override
	public Set<Currency> getCurrencies()
	{
		if( currencies == null )
		{
			synchronized( currencyLock )
			{
				if( currencies == null )
				{
					final Set<Currency> tempCurrencies = Sets.newHashSet();
					final String[] countries = Locale.getISOCountries();
					for( String country : countries )
					{
						final Currency currency = Currency.getInstance(new Locale("", country));
						// Antartica doesn't have a currency :)
						if( currency != null )
						{
							tempCurrencies.add(currency);
						}
					}
					currencies = Collections.unmodifiableSet(tempCurrencies);
				}
			}
		}
		return currencies;
	}

	@SuppressWarnings("nls")
	@Transactional
	@Override
	public void changeCurrency(Catalogue catalogue, Region region, Currency currency)
	{
		if( catalogue != null || region != null )
		{
			throw new IllegalArgumentException("Currently the catalogue and region parameters must be null");
		}

		// priceDao.listByCatalogueAndRegion(null, null);
		final List<Price> prices = priceDao.enumerateForInstitution();
		for( Price price : prices )
		{
			price.setCurrency(currency);
			priceDao.save(price);
		}
	}

	private List<Price> getRegionReferences(Region region)
	{
		return priceDao.enumerateByRegion(region);
	}

	@Override
	@Transactional
	public void addRegionReferencingClasses(Region region, List<Class<?>> referencingClasses)
	{
		if( !getRegionReferences(region).isEmpty() )
		{
			referencingClasses.add(Price.class);
		}
	}

	@Override
	@Transactional
	public void removeRegionReferences(Region region)
	{
		for( Price price : getRegionReferences(region) )
		{
			priceDao.delete(price);
		}
	}

	@Override
	@Transactional
	public void removePricingTierReferences(PricingTier tier)
	{
		if( tier.isPurchase() )
		{
			final Price price = priceDao.getForPurchaseTier(tier);
			if( price != null )
			{
				priceDao.delete(price);
			}
		}
		else
		{
			final List<Price> priceList = priceDao.enumerateAllForSubscriptionTier(tier);
			for( Price price : priceList )
			{
				priceDao.delete(price);
			}
		}
	}

	@Override
	@Transactional
	public void removeCatalogueReferences(Catalogue catalogue)
	{
		for( Price price : priceDao.enumerateByCatalogue(catalogue) )
		{
			priceDao.delete(price);
		}
	}

	@Override
	@Transactional
	public void addCatalogueReferencingClasses(Catalogue catalogue, List<Class<?>> referencingClasses)
	{
		if( !priceDao.enumerateByCatalogue(catalogue).isEmpty() )
		{
			referencingClasses.add(Price.class);
		}
	}

	@Override
	public Date getEndDateOfSubscriptionPeriod(Date startDate, SubscriptionPeriod subscriptionPeriod)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);

		int numberOf = subscriptionPeriod.getDuration();

		switch( subscriptionPeriod.getDurationUnit() )
		{
			case DAYS:
				cal.add(Calendar.DAY_OF_YEAR, numberOf);
				break;
			case WEEKS:
				cal.add(Calendar.WEEK_OF_MONTH, numberOf);
				break;
			case MONTHS:
				cal.add(Calendar.MONTH, numberOf);
				break;
			case YEARS:
				cal.add(Calendar.YEAR, numberOf);
				break;

		}
		return cal.getTime();
	}

	@Override
	@Transactional
	public void recordHarvest(String itemUuid, int version, Sale sale)
	{
		recordHarvest(itemUuid, version, null, sale);
	}

	@Override
	@Transactional
	public void recordHarvest(String itemUuid, int version, String attachUuid, Sale sale)
	{
		StoreHarvestInfo storeHarvestInfo = new StoreHarvestInfo();
		storeHarvestInfo.setItemUuid(itemUuid);
		storeHarvestInfo.setVersion(version);
		storeHarvestInfo.setDate(new Date());
		storeHarvestInfo.setSale(sale);
		storeHarvestInfo.setAttachmentUuid(attachUuid);
		storeHarvestInfoDao.saveOrUpdate(storeHarvestInfo);
	}

	@Override
	public List<StoreHarvestInfo> getHarvestHistoryForSaleItem(String itemUuid)
	{
		List<Criterion> critList = new ArrayList<Criterion>();
		critList.add(Restrictions.eq(StoreHarvestInfo.ITEM_CRITERIA, itemUuid));
		Criterion[] criteria = critList.toArray(new Criterion[critList.size()]);
		return storeHarvestInfoDao.findAllByCriteria(Order.desc(StoreHarvestInfo.SORTABLE_DATE), -1, criteria);
	}
}
