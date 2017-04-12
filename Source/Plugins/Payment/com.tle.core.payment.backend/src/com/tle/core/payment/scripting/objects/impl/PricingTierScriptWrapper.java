package com.tle.core.payment.scripting.objects.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Price;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.scripting.SaveNewPricingAssignmentOperation;
import com.tle.core.payment.scripting.objects.PricingTierScriptObject;
import com.tle.core.payment.scripting.objects.impl.CatalogueScriptWrapper.CatalogueScriptTypeImpl;
import com.tle.core.payment.scripting.types.BaseTierScriptType;
import com.tle.core.payment.scripting.types.CatalogueScriptType;
import com.tle.core.payment.scripting.types.PriceScriptType;
import com.tle.core.payment.scripting.types.PurchaseTierScriptType;
import com.tle.core.payment.scripting.types.RegionScriptType;
import com.tle.core.payment.scripting.types.SubscriptionPeriodScriptType;
import com.tle.core.payment.scripting.types.SubscriptionTierScriptType;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemService;
import com.tle.web.scripting.impl.AbstractScriptWrapper;
import com.tle.web.wizard.WizardState;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class PricingTierScriptWrapper extends AbstractScriptWrapper implements PricingTierScriptObject
{
	private static final long serialVersionUID = 3363732057302309744L;

	/**
	 * The current item the script applies to
	 */
	private Item item;
	private PricingTierAssignment savedPricingTierAssignment;
	private final ConfigurationService configService;
	private final PricingTierService tierService;
	private final ItemService itemService;
	private final WizardState wizardState;

	public PricingTierScriptWrapper(PricingTierService tierService, ItemService itemService,
		ConfigurationService configService, Item item, WizardState wizardState)
	{
		this.tierService = tierService;
		this.itemService = itemService;
		this.configService = configService;
		this.item = item;
		this.wizardState = wizardState;
	}

	/**
	 * List all available purchase tiers
	 */
	@Override
	public List<PurchaseTierScriptType> listAllPurchaseTiers()
	{
		List<PurchaseTierScriptType> retList = new ArrayList<PurchaseTierScriptType>();
		for( PricingTier pTier : tierService.enumerate() )
		{
			if( pTier.isPurchase() )
			{
				Price price = tierService.getPriceForPurchaseTier(pTier);
				PurchaseTierScriptTypeImpl pricingScriptType = new PurchaseTierScriptTypeImpl(pTier, price);
				retList.add(pricingScriptType);
			}
		}
		return retList;
	}

	@Override
	public PurchaseTierScriptType[] getAllPurchaseTiers()
	{
		List<PurchaseTierScriptType> tiers = listAllPurchaseTiers();
		return tiers.toArray(new PurchaseTierScriptType[tiers.size()]);
	}

	@Override
	public List<SubscriptionTierScriptType> listAllSubscriptionTiers()
	{
		List<SubscriptionTierScriptType> retList = new ArrayList<SubscriptionTierScriptType>();
		for( PricingTier pTier : tierService.enumerate() )
		{
			if( !pTier.isPurchase() )
			{
				final SubscriptionTierScriptType subsScriptType = new SubscriptionTierScriptTypeImpl(pTier,
					tierService.enumeratePricesForSubscriptionTier(pTier));
				retList.add(subsScriptType);
			}
		}
		return retList;
	}

	@Override
	public SubscriptionTierScriptType[] getAllSubscriptionTiers()
	{
		List<SubscriptionTierScriptType> tiers = listAllSubscriptionTiers();
		return tiers.toArray(new SubscriptionTierScriptType[tiers.size()]);
	}

	@Override
	public PurchaseTierScriptType getPurchaseTier()
	{
		if( item != null && !item.isNewItem() )
		{
			return getPurchaseTier(new ItemIdKey(item));
		}
		return null;
	}

	@Override
	public PurchaseTierScriptType getPurchaseTier(String uuid)
	{
		PricingTier pTier = tierService.getByUuid(uuid);
		if( pTier.isPurchase() )
		{
			Price price = tierService.getPriceForPurchaseTier(pTier);
			return new PurchaseTierScriptTypeImpl(pTier, price);
		}
		return null;
	}

	@Override
	public PurchaseTierScriptType getPurchaseTier(ItemScriptType itemScriptType)
	{
		return getPurchaseTier(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()));
	}

	/**
	 * Helper method to put together a Tier script bean from an item key
	 * 
	 * @param itemKey
	 * @return
	 */
	private PurchaseTierScriptType getPurchaseTier(ItemKey itemKey)
	{
		PricingTierAssignment assignment = tierService.getPricingTierAssignmentForItem(itemKey);
		if( assignment != null )
		{
			PricingTier theTier = assignment.getPurchasePricingTier();
			if( theTier != null )
			{
				Price price = tierService.getPriceForPurchaseTier(theTier);
				return new PurchaseTierScriptTypeImpl(theTier, price);
			}
		}
		return null;
	}

	@Override
	public void setPurchaseTier(PurchaseTierScriptType purchaseTierValues)
	{
		setPurchaseTier(item, purchaseTierValues);
	}

	@Override
	public void setPurchaseTierByUuid(String purchaseTierUuid)
	{
		setPricingTier(item, purchaseTierUuid);
	}

	@Override
	public void setPurchaseTier(ItemScriptType itemScriptType, PurchaseTierScriptType purchaseTierValues)
	{
		Item selectedItem = itemService.get(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()));
		setPurchaseTier(selectedItem, purchaseTierValues);
	}

	private void setPurchaseTier(Item item, PurchaseTierScriptType purchaseTierValues)
	{
		String pricingTierUuid = purchaseTierValues.getUuid();
		if( !Check.isEmpty(pricingTierUuid) )
		{
			setPricingTier(item, pricingTierUuid);
		}
		else
		{
			// is there any point searching for matching price information here
			// ?
			throw new RuntimeException(
				"Unimplemented action (identifying purchase tier from its plain values instead of its UUID)");
		}
	}

	@Override
	public SubscriptionTierScriptType getSubscriptionTier()
	{
		if( item != null && !item.isNewItem() )
		{
			return getSubscriptionTier(new ItemIdKey(item));
		}
		return null;
	}

	/**
	 * @param uuid pricingTier identifier
	 * @return a single SubscriptionTier by uuid if it exists and is not a
	 *         PurchaseTier, otherwise null
	 */
	@Override
	public SubscriptionTierScriptType getSubscriptionTier(String uuid)
	{
		PricingTier pTier = tierService.getByUuid(uuid);
		if( !pTier.isPurchase() )
		{
			return new SubscriptionTierScriptTypeImpl(pTier, tierService.enumeratePricesForSubscriptionTier(pTier));
		}
		return null;
	}

	/**
	 * @param itemScriptType an ItemScriptType expected to contain an Item
	 *            identifier
	 * @return a single SubscriptionTier by uuid if it exists and is not a
	 *         PurchaseTier, otherwise null
	 */
	@Override
	public SubscriptionTierScriptType getSubscriptionTier(ItemScriptType itemScriptType)
	{
		return getSubscriptionTier(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()));
	}

	/**
	 * Helper method for the common logic of constructing SubscriptionScriptType
	 * for the specified item
	 * 
	 * @param itemKey identifier for an arbitrary Item
	 * @return subscriptionTier if it exists for the Item, otherwise null
	 */
	private SubscriptionTierScriptType getSubscriptionTier(ItemKey itemKey)
	{
		PricingTierAssignment assignment = tierService.getPricingTierAssignmentForItem(itemKey);
		if( assignment != null )
		{
			PricingTier pTier = assignment.getSubscriptionPricingTier();
			if( pTier != null )
			{
				return new SubscriptionTierScriptTypeImpl(pTier, tierService.enumeratePricesForSubscriptionTier(pTier));
			}
		}
		return null;
	}

	@Override
	public void setSubscriptionTier(SubscriptionTierScriptType sScriptType)
	{
		setPricingTier(item, sScriptType.getUuid());
	}

	@Override
	public void setSubscriptionTierByUuid(String subscriptionTierUuid)
	{
		setPricingTier(item, subscriptionTierUuid);
	}

	@Override
	public void setSubscriptionTier(ItemScriptType itemScriptType, SubscriptionTierScriptType sScriptType)
	{
		Item selectedItem = itemService.get(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()));
		setPricingTier(selectedItem, sScriptType.getUuid());
	}

	/**
	 * Private helper method for the common logic of creating and adding a
	 * purchase assignment to a specified item
	 * 
	 * @param item
	 * @param pricingTierUuid
	 */
	private void setPricingTier(Item item, String pricingTierUuid)
	{
		PricingTier pricingTier = tierService.getByUuid(pricingTierUuid);

		if( pricingTier == null )
		{
			throw new RuntimeException("Could not identify any existing Subscription Tier using UUID "
				+ pricingTierUuid);
		}

		if( item.isNewItem() )
		{
			addSaveOperationToWizardState(pricingTier);
		}
		else
		{
			// Persisting an assignment instance where the current item already
			// persists in the database, in other words when the wizard is in
			// edit mode, not when it's in create new item mode.
			ItemKey itemId = new ItemIdKey(item);
			PricingTierAssignment existingAssignment = tierService.getPricingTierAssignmentForItem(itemId);
			if( existingAssignment != null )
			{
				if( pricingTier.isPurchase() )
				{
					existingAssignment.setPurchasePricingTier(pricingTier);
				}
				else
				{
					existingAssignment.setSubscriptionPricingTier(pricingTier);
				}

				tierService.savePricingTierAssignment(existingAssignment);
			}
			else
			{
				if( pricingTier.isPurchase() )
				{
					tierService.createPricingTierAssignment(itemId, pricingTier, null, false);
				}
				else
				{
					tierService.createPricingTierAssignment(itemId, null, pricingTier, false);
				}
			}
		}
	}

	@Override
	public boolean isFreeAllowed()
	{
		return isFreeAllowed(new ItemId(item.getUuid(), item.getVersion()));
	}

	@Override
	public boolean isFreeAllowed(ItemScriptType itemScriptType)
	{
		return isFreeAllowed(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()));
	}

	/**
	 * @param itemKey
	 * @return Where a pricingAssignment exists for the specified item, return
	 *         its free flag, otherwise return false
	 */
	private boolean isFreeAllowed(ItemKey itemKey)
	{
		PricingTierAssignment pricingTierAssignment = tierService.getPricingTierAssignmentForItem(itemKey);
		return pricingTierAssignment != null ? pricingTierAssignment.isFreeItem() : false;
	}

	/**
	 * Set the free flag on a purchase tier assignment (for the current item)
	 */
	@Override
	public void setFreeAllowed(boolean free)
	{
		setFreeAllowed(new ItemId(item.getUuid(), item.getVersion()), free);
	}

	/**
	 * Set the free flag on a purchase tier assignment (for a specified item)
	 */
	@Override
	public void setFreeAllowed(ItemScriptType itemScriptType, boolean free)
	{
		setFreeAllowed(new ItemId(itemScriptType.getUuid(), itemScriptType.getVersion()), free);
	}

	private void setFreeAllowed(ItemKey itemId, boolean free)
	{
		if( item.isNewItem() )
		{
			addSaveOperationToWizardState(null, true, free);
			return;
		}

		PricingTierAssignment pricingTierAssignment = tierService.getPricingTierAssignmentForItem(itemId);
		if( pricingTierAssignment == null )
		{
			tierService.createPricingTierAssignment(itemId, null, null, free);
		}
		else
		{
			pricingTierAssignment.setFreeItem(free);
		}
	}

	@Override
	public boolean isFreeEnabled()
	{
		PaymentSettings settings = configService.getProperties(new PaymentSettings());
		return settings.isFreeEnabled();
	}

	@Override
	public boolean isPurchaseEnabled()
	{
		PaymentSettings settings = configService.getProperties(new PaymentSettings());
		return settings.isPurchaseEnabled();
	}

	@Override
	public boolean isSubscriptionEnabled()
	{
		PaymentSettings settings = configService.getProperties(new PaymentSettings());
		return settings.isSubscriptionEnabled();
	}

	private void addSaveOperationToWizardState(PricingTier pricingTier)
	{
		addSaveOperationToWizardState(pricingTier, false, false);
	}

	/**
	 * Extract the wizard state from the wizard page object which we require to
	 * have passed in from the scripting environment, and create and add a
	 * SaveOperation to be added to the wizard's on-save behaviour.
	 * 
	 * @param pageScriptObj
	 * @param pricingTier
	 * @param changeFree Things that set pricing tiers shouldn't change the
	 *            items free-ness
	 */
	private void addSaveOperationToWizardState(PricingTier pricingTier, boolean changeFree, boolean free)
	{
		if( wizardState != null )
		{
			savedPricingTierAssignment = savedPricingTierAssignment == null ? new PricingTierAssignment()
				: savedPricingTierAssignment;
			PricingTierAssignment pricingTierAssignment = savedPricingTierAssignment;
			pricingTierAssignment.setItem(item);
			if( pricingTier != null && pricingTier.isPurchase() )
			{
				pricingTierAssignment.setPurchasePricingTier(pricingTier);
			}
			else if( pricingTier != null )
			{
				pricingTierAssignment.setSubscriptionPricingTier(pricingTier);
			}

			if( changeFree )
			{
				pricingTierAssignment.setFreeItem(free);
			}
			wizardState.setWizardSaveOperation(Constants.BLANK, new SaveNewPricingAssignmentOperation(
				pricingTierAssignment));
		}
		else
		{
			throw new RuntimeException("Cannot create save operation: failed to get WizardState");
		}
	}

	protected static abstract class BaseTierScriptTypeImpl implements BaseTierScriptType
	{
		protected final PricingTier tier;

		protected BaseTierScriptTypeImpl(PricingTier tier)
		{
			this.tier = tier;
		}

		@Override
		public String getUniqueID()
		{
			return getUuid();
		}

		@Override
		public String getUuid()
		{
			return tier.getUuid();
		}

		@Override
		public String getName()
		{
			return CurrentLocale.get(tier.getName(), tier.getUuid());
		}

		@Override
		public String getDescription()
		{
			return CurrentLocale.get(tier.getDescription());
		}
	}

	public static class PurchaseTierScriptTypeImpl extends BaseTierScriptTypeImpl implements PurchaseTierScriptType
	{
		private static final long serialVersionUID = -959840711848026441L;

		private final Price price;

		public PurchaseTierScriptTypeImpl(PricingTier tier, Price price)
		{
			super(tier);
			this.price = price;
		}

		@Override
		public PriceScriptType getPrice()
		{
			return new PriceScriptTypeImpl(price);
		}
	}

	public static class SubscriptionTierScriptTypeImpl extends BaseTierScriptTypeImpl
		implements
			SubscriptionTierScriptType
	{
		private static final long serialVersionUID = 8794277526276445648L;

		private final List<Price> pricesPeriods;

		public SubscriptionTierScriptTypeImpl(PricingTier tier, List<Price> pricesPeriods)
		{
			super(tier);
			this.pricesPeriods = pricesPeriods;
		}

		@Override
		public List<PriceScriptType> getPrices()
		{
			return Lists.newArrayList(Lists.transform(pricesPeriods, new Function<Price, PriceScriptType>()
			{
				@Override
				public PriceScriptType apply(Price price)
				{
					return new PriceScriptTypeImpl(price);
				}
			}));
		}
	}

	public static final class PriceScriptTypeImpl implements PriceScriptType
	{
		private static final long serialVersionUID = 1L;

		private final Price price;

		public PriceScriptTypeImpl(Price price)
		{
			this.price = price;
		}

		@Override
		public long getRawValue()
		{
			return price.getValue();
		}

		@Override
		public String getCurrency()
		{
			return price.getCurrency().getCurrencyCode();
		}

		@Override
		public double getValue()
		{
			int digits = price.getCurrency().getDefaultFractionDigits();
			BigDecimal adjusted = new BigDecimal(price.getValue());
			adjusted = adjusted.movePointLeft(digits);
			return adjusted.doubleValue();
		}

		@Override
		public SubscriptionPeriodScriptType getSubscriptionPeriod()
		{
			final SubscriptionPeriod period = price.getPeriod();
			if( period == null )
			{
				return null;
			}
			return new SubscriptionPeriodScriptTypeImpl(price.getPeriod());
		}

		@Override
		public CatalogueScriptType getCatalogue()
		{
			final Catalogue cat = price.getCatalogue();
			if( cat == null )
			{
				return null;
			}
			return new CatalogueScriptTypeImpl(cat);
		}

		@Override
		public RegionScriptType getRegion()
		{
			final Region region = price.getRegion();
			if( region == null )
			{
				return null;
			}
			return new RegionScriptTypeImpl(region);
		}
	}

	public static class SubscriptionPeriodScriptTypeImpl implements SubscriptionPeriodScriptType
	{
		private final SubscriptionPeriod period;

		protected SubscriptionPeriodScriptTypeImpl(SubscriptionPeriod period)
		{
			this.period = period;
		}

		@Override
		public String getUuid()
		{
			return period.getUuid();
		}

		@Override
		public String getName()
		{
			return CurrentLocale.get(period.getName(), period.getUuid());
		}

		@Override
		public int getDuration()
		{
			return period.getDuration();
		}

		@Override
		public String getDurationUnit()
		{
			return period.getDurationUnit().toString();
		}
	}

	public static class RegionScriptTypeImpl implements RegionScriptType
	{
		private final Region region;

		protected RegionScriptTypeImpl(Region region)
		{
			this.region = region;
		}

		@Override
		public String getUniqueID()
		{
			return getUuid();
		}

		@Override
		public String getUuid()
		{
			return region.getUuid();
		}

		@Override
		public String getName()
		{
			return CurrentLocale.get(region.getName(), region.getUuid());
		}

		@Override
		public String getDescription()
		{
			return CurrentLocale.get(region.getDescription());
		}

		@Override
		public List<String> listCountries()
		{
			return Lists.newArrayList(region.getCountries());
		}

		@Override
		public String[] getCountries()
		{
			List<String> countries = listCountries();
			return countries.toArray(new String[countries.size()]);
		}
	}
}
