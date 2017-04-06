package com.tle.core.payment.scripting.objects.impl;

import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.Region;
import com.tle.core.payment.scripting.objects.CatalogueScriptObject;
import com.tle.core.payment.scripting.objects.impl.PricingTierScriptWrapper.RegionScriptTypeImpl;
import com.tle.core.payment.scripting.types.CatalogueScriptType;
import com.tle.core.payment.scripting.types.RegionScriptType;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.CatalogueService.CatalogueInfo;
import com.tle.web.scripting.impl.AbstractScriptWrapper;

/**
 * @author larry
 */
public class CatalogueScriptWrapper extends AbstractScriptWrapper implements CatalogueScriptObject
{
	/**
	 * The current item the script applies to
	 */
	private Item item;
	private final CatalogueService catalogueService;

	public CatalogueScriptWrapper(CatalogueService catalogueService, Item item)
	{
		this.catalogueService = catalogueService;
		this.item = item;
	}

	/**
	 * Return all catalogues in the system (enabled or otherwise)
	 */
	@Override
	public List<CatalogueScriptType> listAllCatalogues()
	{
		return transformList(catalogueService.enumerate());
	}

	/**
	 * The list of catalogues this item appears in due to be added to the
	 * whitelist.
	 * 
	 * @return if the item is new, return an empty list, otherwise perform
	 *         catalogue search
	 */
	@Override
	public List<CatalogueScriptType> listWhitelistCatalogues()
	{
		if( item.isNewItem() )
		{
			return Lists.newArrayList();
		}
		else
		{
			final CatalogueInfo catInfo = catalogueService.groupCataloguesForItem(item);
			return transformList(catInfo.getWhitelist());
		}
	}

	/**
	 * The list of catalogues this item appears in due to be added to the
	 * blacklist
	 * 
	 * @return if the item is new, return an empty list, otherwise perform
	 *         catalogue search
	 */
	@Override
	public List<CatalogueScriptType> listBlacklistCatalogues()
	{
		if( item.isNewItem() )
		{
			return Lists.newArrayList();
		}
		else
		{
			final CatalogueInfo catInfo = catalogueService.groupCataloguesForItem(item);
			return transformList(catInfo.getBlacklist());
		}
	}

	/**
	 * The list of catalogues this item appears in due to be found in the
	 * associated dynamic collection.
	 * 
	 * @return if the item is new, return an empty list, otherwise perform
	 *         catalogue search
	 */
	@Override
	public List<CatalogueScriptType> listDynamicCatalogues()
	{
		if( item.isNewItem() )
		{
			return Lists.newArrayList();
		}
		else
		{
			final CatalogueInfo catInfo = catalogueService.groupCataloguesForItem(item);
			return transformList(catInfo.getDynamic());
		}
	}

	private List<CatalogueScriptType> transformList(List<Catalogue> catalogues)
	{
		// Create a copy of transformed list as transform runs multiple times
		// otherwise
		return Lists.newArrayList(Lists.transform(catalogues, new Function<Catalogue, CatalogueScriptType>()
		{
			@Override
			public CatalogueScriptType apply(Catalogue input)
			{
				return new CatalogueScriptTypeImpl(input);
			}
		}));
	}

	@Override
	public void addToWhitelist(CatalogueScriptType catalogueScriptType)
	{
		CatalogueScriptTypeImpl catImpl = (CatalogueScriptTypeImpl) catalogueScriptType;
		catalogueService.addItemToList(catImpl.getCatalogue().getId(), item, false);
	}

	@Override
	public void addToBlacklist(CatalogueScriptType catalogueScriptType)
	{
		CatalogueScriptTypeImpl catImpl = (CatalogueScriptTypeImpl) catalogueScriptType;
		catalogueService.addItemToList(catImpl.getCatalogue().getId(), item, true);
	}

	@Override
	public void removeFromWhitelist(CatalogueScriptType catalogueScriptType)
	{
		CatalogueScriptTypeImpl catImpl = (CatalogueScriptTypeImpl) catalogueScriptType;
		catalogueService.removeItemFromList(catImpl.getCatalogue().getId(), item, false);
	}

	@Override
	public void removeFromBlacklist(CatalogueScriptType catalogueScriptType)
	{
		CatalogueScriptTypeImpl catImpl = (CatalogueScriptTypeImpl) catalogueScriptType;
		catalogueService.removeItemFromList(catImpl.getCatalogue().getId(), item, true);
	}

	@Override
	public CatalogueScriptType getCatalogueByUuid(String uuid)
	{
		Catalogue catalogue = catalogueService.getByUuid(uuid);
		if( catalogue == null )
		{
			return null;
		}
		return new CatalogueScriptTypeImpl(catalogue);
	}

	public static final class CatalogueScriptTypeImpl implements CatalogueScriptType
	{
		private final Catalogue catalogue;

		public CatalogueScriptTypeImpl(Catalogue catalogue)
		{
			this.catalogue = catalogue;
		}

		@Override
		public String getUniqueID()
		{
			return getUuid();
		}

		@Override
		public String getUuid()
		{
			return catalogue.getUuid();
		}

		@Override
		public String getName()
		{
			return CurrentLocale.get(catalogue.getName(), catalogue.getUuid());
		}

		@Override
		public String getDescription()
		{
			return CurrentLocale.get(catalogue.getDescription());
		}

		@Override
		public boolean isEnabled()
		{
			return !catalogue.isDisabled();
		}

		@Override
		public boolean isRegionRestricted()
		{
			return catalogue.isRegionRestricted();
		}

		@Override
		public List<RegionScriptType> listRegions()
		{
			final Set<Region> regions = catalogue.getRegions();
			if( regions == null )
			{
				return Lists.newArrayList();
			}
			return Lists.newArrayList(Lists.transform(Lists.newArrayList(regions),
				new Function<Region, RegionScriptType>()
				{
					@Override
					public RegionScriptType apply(Region region)
					{
						return new RegionScriptTypeImpl(region);
					}
				}));
		}

		@Override
		public RegionScriptType[] getRegions()
		{
			List<RegionScriptType> regions = listRegions();
			return regions.toArray(new RegionScriptType[regions.size()]);
		}

		/**
		 * Internal use only
		 * 
		 * @return
		 */
		public Catalogue getCatalogue()
		{
			return catalogue;
		}
	}
}
