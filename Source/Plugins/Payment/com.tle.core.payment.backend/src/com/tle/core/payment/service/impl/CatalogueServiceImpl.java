package com.tle.core.payment.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.Region;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.search.PresetSearch;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.dynacollection.DynaCollectionReferencesListener;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.events.ItemDeletedEvent;
import com.tle.core.events.listeners.ItemDeletedListener;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.PaymentIndexFields;
import com.tle.core.payment.dao.CatalogueAssignmentDao;
import com.tle.core.payment.dao.CatalogueDao;
import com.tle.core.payment.dao.RegionDao;
import com.tle.core.payment.events.CatalogueDeletionEvent;
import com.tle.core.payment.events.listeners.RegionDeletionListener;
import com.tle.core.payment.events.listeners.RegionReferencesListener;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.CatalogueEditingSession;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.filters.SelectedItemsFilter;
import com.tle.core.workflow.operations.WorkflowFactory;

@Bind(CatalogueService.class)
@Singleton
@SecureEntity(Catalogue.ENTITY_TYPE)
public class CatalogueServiceImpl extends AbstractEntityServiceImpl<CatalogueEditingBean, Catalogue, CatalogueService>
	implements
		CatalogueService,
		RegionDeletionListener,
		RegionReferencesListener,
		ItemDeletedListener,
		DynaCollectionReferencesListener
{
	private final CatalogueDao catDao;

	@Inject
	private RegionDao regionDao;
	@Inject
	private CatalogueAssignmentDao assignmentDao;

	@Inject
	private RegionService regionService;
	@Inject
	private DynaCollectionService dynamicCollectionService;
	@Inject
	private FreeTextService freeText;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private PricingTierService tierService;

	@Inject
	public CatalogueServiceImpl(CatalogueDao catDao)
	{
		super(Node.CATALOGUE, catDao);
		this.catDao = catDao;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<CatalogueEditingBean, Catalogue>> SESSION createSession(
		String sessionId, EntityPack<Catalogue> pack, CatalogueEditingBean bean)
	{
		return (SESSION) new CatalogueEditingSession(sessionId, pack, bean);
	}

	@Override
	protected void doValidation(EntityEditingSession<CatalogueEditingBean, Catalogue> session, Catalogue entity,
		List<ValidationError> errors)
	{
		// Nothing to validate?
	}

	@Override
	protected void doValidationBean(CatalogueEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected CatalogueEditingBean createEditingBean()
	{
		return new CatalogueEditingBean();
	}

	@Override
	protected void populateEditingBean(CatalogueEditingBean catBean, Catalogue entity)
	{
		super.populateEditingBean(catBean, entity);

		catBean.setEnabled(!entity.isDisabled());

		catBean.setRegionFiltered(entity.isRegionRestricted());
		final Set<Region> regions = entity.getRegions();
		final List<Long> beanRegions = catBean.getRegions();
		beanRegions.clear();
		beanRegions.addAll(Collections2.transform(regions, new Function<Region, Long>()
		{
			@Override
			public Long apply(Region input)
			{
				return input.getId();
			}
		}));
		catBean.setDynamicCollection(entity.getDynamicCollection() == null ? null : entity.getDynamicCollection()
			.getId());

		final TargetList targets = aclManager.getTargetList(Node.CATALOGUE, entity);
		for( TargetListEntry target : targets.getEntries() )
		{
			if( target.getPrivilege().equals(PaymentConstants.PRIV_MANAGE_CATALOGUE) )
			{
				catBean.setManageCatalogueExpression(target.getWho());
				break;
			}
		}
	}

	@Override
	protected void populateEntity(CatalogueEditingBean catBean, Catalogue entity)
	{
		super.populateEntity(catBean, entity);

		entity.setRegionRestricted(catBean.isRegionFiltered());
		final List<Long> newRegions = catBean.getRegions();
		final Set<Region> persistedRegions = entity.getRegions();
		for( Iterator<Region> it = persistedRegions.iterator(); it.hasNext(); )
		{
			final Region region = it.next();
			if( !newRegions.contains(region.getId()) )
			{
				it.remove();
			}
			else
			{
				newRegions.remove(region.getId());
			}
		}
		final List<Region> addedRegions = regionDao.getByIds(newRegions);
		persistedRegions.addAll(addedRegions);

		final DynaCollection dynamic = catBean.getDynamicCollection() == null ? null : dynamicCollectionService
			.get(catBean.getDynamicCollection());
		entity.setDynamicCollection(dynamic);
	}

	@Override
	protected void saveTargetLists(EntityEditingSession<CatalogueEditingBean, Catalogue> session,
		EntityPack<Catalogue> pack)
	{
		final TargetList list = new TargetList();
		final List<TargetListEntry> entries = Lists.newArrayList();
		list.setEntries(entries);
		final CatalogueEditingBean catBean = session.getBean();
		final String manageExpression = catBean.getManageCatalogueExpression();
		if( manageExpression != null )
		{
			entries.add(new TargetListEntry(true, false, PaymentConstants.PRIV_MANAGE_CATALOGUE, manageExpression));
		}
		pack.setTargetList(list);

		super.saveTargetLists(session, pack);
	}

	@Override
	@Transactional
	public List<CatalogueAssignment> listCataloguesForItem(Item item)
	{
		return assignmentDao.enumerateForItem(item);
	}

	@Override
	@Transactional
	public List<CatalogueAssignment> listItemsForCatalogue(Catalogue cat)
	{
		return assignmentDao.enumerateForCatalogue(cat);
	}

	@Override
	@Transactional
	public CatalogueInfo groupCataloguesForItem(Item item)
	{
		final CatalogueInfoImpl cg = new CatalogueInfoImpl();
		cg.setItem(item);

		final List<Catalogue> wl = Lists.newArrayList();
		final List<Catalogue> bl = Lists.newArrayList();
		final List<Catalogue> dyn = Lists.newArrayList();
		final List<Catalogue> dynExWl = Lists.newArrayList();
		final List<Catalogue> none = Lists.newArrayList();
		cg.setWhitelist(wl);
		cg.setBlacklist(bl);
		cg.setDynamic(dyn);
		cg.setDynamicExWhitelist(dynExWl);
		cg.setNone(none);

		final Map<Long, CatalogueAssignment> catMap = Maps.uniqueIndex(listCataloguesForItem(item),
			new Function<CatalogueAssignment, Long>()
			{
				@Override
				public Long apply(CatalogueAssignment input)
				{
					return input.getCatalogue().getId();
				}
			});

		for( Catalogue cat : catDao.enumerateAll() )
		{
			final long catId = cat.getId();
			final CatalogueAssignment catAss = catMap.get(catId);
			boolean inWl = false;
			if( catAss != null )
			{
				inWl = !catAss.isBlacklisted();
				(inWl ? wl : bl).add(cat);
			}

			final PresetSearch search = createSearch(cat, false);
			search.setPrivilege(null);
			search.addMust(FreeTextQuery.FIELD_ID, Long.toString(item.getId()));
			if( freeText.search(search, 0, 1).getAvailable() > 0 )
			{
				dyn.add(cat);
				if( !inWl )
				{
					dynExWl.add(cat);
				}
			}
			else if( !inWl )
			{
				none.add(cat);
			}
		}
		return cg;
	}

	@Override
	public List<Catalogue> enumerateForCountry(String country)
	{
		final List<Catalogue> enabled = enumerateEnabled();
		final List<Catalogue> filtered = Lists.newArrayList();
		for( Catalogue cat : enabled )
		{
			if( !cat.isRegionRestricted() || regionService.containsCountry(cat.getRegions(), country) )
			{
				filtered.add(cat);
			}
		}
		return filtered;
	}

	@Override
	@Transactional
	public boolean addItemToList(Long catalogueId, Item item, boolean black)
	{
		boolean modified = false;
		Catalogue catalogue = catDao.findById(catalogueId);
		modified = assignmentDao.addToList(catalogue, item, black);

		return modified;
	}

	@Override
	@Transactional
	public boolean removeItemFromList(Long catalogueId, Item item, boolean black)
	{
		boolean modified = false;
		Catalogue catalogue = catDao.findById(catalogueId);
		modified = assignmentDao.removeFromList(catalogue, item);

		return modified;
	}

	/**
	 * The intent here is to create a search which can (optionally) be composed
	 * into a larger search.
	 * 
	 * @param catalogueUuid
	 * @return expandable PresetSearch
	 */
	@Override
	public PresetSearch createLiveSearch(String catalogueUuid, StoreFront storefront)
	{
		FreeTextQuery expandableQuery = buildCatalogueSearchQuery(catalogueUuid, false);

		FreeTextQuery isPurchaseableOrFree = storefront == null ? tierService.getPriceSetQuery(true) : tierService
			.getPriceSetQuery(true, storefront);

		if( expandableQuery != null )
		{
			expandableQuery = new FreeTextBooleanQuery(false, true, expandableQuery, isPurchaseableOrFree);
		}
		else
		{
			expandableQuery = isPurchaseableOrFree;
		}

		expandableQuery = addNotBlacklisted(expandableQuery, catalogueUuid);

		return new PresetSearch(null, expandableQuery, true);
	}

	@Override
	public PresetSearch createLiveSearch(String catalogueUuid)
	{
		return createLiveSearch(catalogueUuid, null);
	}

	/**
	 * Not adding purchasability, and including non-live
	 */
	@Override
	public PresetSearch createSearch(String catalogueUuid, boolean excludeWhitelist)
	{
		FreeTextQuery catalogueSearchQuery = buildCatalogueSearchQuery(catalogueUuid, excludeWhitelist);
		catalogueSearchQuery = addNotBlacklisted(catalogueSearchQuery, catalogueUuid);

		return new PresetSearch(null, catalogueSearchQuery, false);
	}

	@Override
	public PresetSearch createSearch(Catalogue catalogue, boolean excludeWhitelist)
	{
		FreeTextQuery catalogueSearchQuery = buildCatalogueSearchQuery(catalogue, excludeWhitelist);
		catalogueSearchQuery = addNotBlacklisted(catalogueSearchQuery, catalogue.getUuid());

		return new PresetSearch(null, catalogueSearchQuery, false);
	}

	private FreeTextQuery buildCatalogueSearchQuery(String catalogueUuid, boolean excludeWhitelist)
	{
		Catalogue catalogue = catDao.getByUuid(catalogueUuid);
		return buildCatalogueSearchQuery(catalogue, excludeWhitelist);
	}

	private FreeTextQuery buildCatalogueSearchQuery(Catalogue catalogue, boolean excludeWhitelist)
	{
		FreeTextQuery expandableQuery = null;

		DynaCollection dc = catalogue.getDynamicCollection();
		FreeTextBooleanQuery isDynamicCompleteDefinition = null;
		if( dc != null )
		{
			String queryText = dc.getFreetextQuery();
			// We want those search clauses (if any) which define the dynamic
			// collection apart from any freetext query (in other words, schemas
			// and collections). The freetext query (if any) is isolated and
			// inserted later.
			FreeTextBooleanQuery isDynamicSearchClause = dynamicCollectionService.getSearchClausesNoVirtualisation(dc);
			boolean hasSearchClauses = !Check.isEmpty(isDynamicSearchClause.getClauses());

			if( Check.isEmpty(queryText) )
			{
				if( hasSearchClauses )
				{
					isDynamicCompleteDefinition = isDynamicSearchClause;
				}
				// else stays null
			}
			else
			{
				// It's a bit tedious to get down to this level of query logic
				// here, but we can't have the dynamic collection's query string
				// (if it exists at all) anywhere else except OR'd with
				// FIELD_WHITELISTED. Most likely we'll be seeing 2 fields,
				// FIELD_NAME_VECTORED & FIELD_BODY

				// FIELD_BODY is separated to FIELD_BODY and
				// FIELD_ATTACHMENT_VECTORED
				FreeTextFieldQuery[] basicNameBodyQueries = new FreeTextFieldQuery[FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS
					.size()];
				for( int i = 0; i < FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS.size(); ++i )
				{
					basicNameBodyQueries[i] = new FreeTextFieldQuery(
						FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS.get(i), queryText);
				}

				FreeTextBooleanQuery basicDynamicQueryText = new FreeTextBooleanQuery(false, false,
					basicNameBodyQueries);

				if( hasSearchClauses )
				{
					isDynamicCompleteDefinition = new FreeTextBooleanQuery(false, true, basicDynamicQueryText,
						isDynamicSearchClause);
				}
				else
				{
					isDynamicCompleteDefinition = basicDynamicQueryText;
				}
			}
		}

		if( !excludeWhitelist )
		{
			FreeTextFieldQuery isCatalogued = new FreeTextFieldQuery(PaymentIndexFields.FIELD_WHITELISTED,
				catalogue.getUuid());
			if( isDynamicCompleteDefinition != null )
			{
				expandableQuery = new FreeTextBooleanQuery(false, false, isDynamicCompleteDefinition, isCatalogued);
			}
			else
			{
				expandableQuery = isCatalogued;
			}
		}
		else
		{
			expandableQuery = isDynamicCompleteDefinition;
		}

		return expandableQuery;
	}

	private FreeTextQuery addNotBlacklisted(FreeTextQuery expandableQuery, String catalogueUuid)
	{
		// create a single -ve field, aka a MUST NOT
		FreeTextBooleanQuery blacklisted = new FreeTextBooleanQuery(true, false, new FreeTextFieldQuery(
			PaymentIndexFields.FIELD_BLACKLISTED, catalogueUuid));

		if( expandableQuery != null )
		{
			// which is AND'd with whatever we passed in
			expandableQuery = new FreeTextBooleanQuery(false, true, expandableQuery, blacklisted);
		}
		else
		{
			// or sent back on its own
			expandableQuery = blacklisted;
		}

		return expandableQuery;
	}

	@Override
	public boolean containsLiveItem(String catalogueUuid, ItemKey itemId, StoreFront storefront)
	{
		final PresetSearch search = createLiveSearch(catalogueUuid);
		search.addMust(FreeTextQuery.FIELD_UUID, itemId.getUuid());
		search.addMust(FreeTextQuery.FIELD_VERSION, Integer.toString(itemId.getVersion()));
		search.setPrivilege(null);
		final int[] counts = freeText.countsFromFilters(Collections.singletonList(search));
		return counts[0] > 0;
	}

	@Override
	protected void deleteReferences(Catalogue entity)
	{
		super.deleteReferences(entity);

		final List<ItemId> itemIds = Lists.newArrayList();
		for( CatalogueAssignment catAss : assignmentDao.enumerateForCatalogue(entity) )
		{
			itemIds.add(catAss.getItem().getItemId());
			assignmentDao.delete(catAss);
		}
		itemService.operateAll(new SelectedItemsFilter(itemIds, workflowFactory.reindexOnly(false)));

		publishEvent(new CatalogueDeletionEvent(entity));
	}

	private List<Catalogue> getRegionReferences(Region region)
	{
		return catDao.enumerateByRegion(region, false);
	}

	@Override
	public void addRegionReferencingClasses(Region region, List<Class<?>> referencingClasses)
	{
		if( !getRegionReferences(region).isEmpty() )
		{
			referencingClasses.add(Catalogue.class);
		}
	}

	@Override
	@Transactional
	public void removeRegionReferences(Region region)
	{
		for( Catalogue cat : getRegionReferences(region) )
		{
			cat.getRegions().remove(region);
			catDao.save(cat);
		}
	}

	@Override
	@SecureOnReturn(priv = PaymentConstants.PRIV_MANAGE_CATALOGUE)
	public List<BaseEntityLabel> listManageable()
	{
		return catDao.listAll(Catalogue.ENTITY_TYPE);
	}

	@Override
	public boolean canManage(BaseEntityLabel entity)
	{
		return canManage((Object) entity);
	}

	@Override
	public boolean canManage(Catalogue entity)
	{
		return canManage((Object) entity);
	}

	private boolean canManage(Object entity)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(PaymentConstants.PRIV_MANAGE_CATALOGUE);
		return !aclManager.filterNonGrantedPrivileges(entity, privs).isEmpty();
	}

	@Override
	@Transactional
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		// Dodgy, but hey, I copied this temp item hack from HierarchyService
		Item item = new Item();
		item.setId(event.getKey());

		for( CatalogueAssignment cat : assignmentDao.enumerateForItem(item) )
		{
			assignmentDao.delete(cat);
		}
	}

	@Override
	public void addDynaCollectionReferencingClasses(DynaCollection dc, List<Class<?>> referencingClasses)
	{
		if( catDao.isExistingReferences(dc) )
		{
			referencingClasses.add(Catalogue.class);
		}
	}

	public static class CatalogueInfoImpl implements CatalogueInfo
	{
		private Item item;
		private List<Catalogue> whitelist;
		private List<Catalogue> blacklist;
		private List<Catalogue> dynamic;
		private List<Catalogue> dynamicExWhitelist;
		private List<Catalogue> none;

		@Override
		public Item getItem()
		{
			return item;
		}

		public void setItem(Item item)
		{
			this.item = item;
		}

		@Override
		public List<Catalogue> getWhitelist()
		{
			return whitelist;
		}

		public void setWhitelist(List<Catalogue> whitelist)
		{
			this.whitelist = whitelist;
		}

		@Override
		public List<Catalogue> getBlacklist()
		{
			return blacklist;
		}

		public void setBlacklist(List<Catalogue> blacklist)
		{
			this.blacklist = blacklist;
		}

		@Override
		public List<Catalogue> getDynamic()
		{
			return dynamic;
		}

		public void setDynamic(List<Catalogue> dynamic)
		{
			this.dynamic = dynamic;
		}

		@Override
		public List<Catalogue> getDynamicExWhitelist()
		{
			return dynamicExWhitelist;
		}

		public void setDynamicExWhitelist(List<Catalogue> dynamicExWhitelist)
		{
			this.dynamicExWhitelist = dynamicExWhitelist;
		}

		@Override
		public List<Catalogue> getNone()
		{
			return none;
		}

		public void setNone(List<Catalogue> none)
		{
			this.none = none;
		}
	}
}
