package com.tle.web.api.payment.store.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.TaxType;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SortField;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.payment.PaymentIndexFields;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StoreCatalogueSearchBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.TaxService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.ItemService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.payment.backend.links.StoreBeanLinkService;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@SuppressWarnings("nls")
@Bind
@Path("store/catalogue")
@Api(value = "/store/catalogue", description = "store-catalogue")
@Produces("application/json")
@Singleton
public class StoreCatalogueResource extends AbstractStoreResource
{
	private static final String KEY_ERROR_NOT_IN_CATALOGUE = "error.itemnotfound.catalogue";

	@Inject
	private CatalogueService catService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private PricingTierService tierService;
	@Inject
	private ItemService itemService;
	@Inject
	private StoreBeanSerializer serializer;
	@Inject
	private PricingTierService pricingTierService;
	@Inject
	private TaxService taxService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private StoreBeanLinkService storeBeanLinkService;
	@Inject
	private UrlLinkService urlLinkService;

	/**
	 * Get a list of catalogues that the current StoreFront can see (country
	 * code filter)
	 */
	@ApiOperation(value = "Get a list of catalogues")
	@GET
	@Path("")
	public List<StoreCatalogueBean> getCatalogues()
	{
		StoreFront sf = getStoreFront();
		// This is the country of the store front making the request
		String sfCountry = sf.getCountry();

		// StoreFront's needs must have a country
		List<StoreCatalogueBean> retlist = new ArrayList<StoreCatalogueBean>();
		List<Catalogue> allCatsForCountry = catService.enumerateForCountry(sfCountry);
		for( Catalogue catalogue : allCatsForCountry )
		{
			final StoreCatalogueBean catBean = convertCatalogueToBean(catalogue);
			final DefaultSearch searchReq = catService.createLiveSearch(catalogue.getUuid(), sf);
			searchReq.setPrivilege(null);
			final int available = freetextService.countsFromFilters(Collections.singleton(searchReq))[0];
			catBean.setAvailable(available);
			retlist.add(catBean);
		}
		return retlist;
	}

	/**
	 * We sanity-check that this catalogue ID is properly visible to the caller,
	 * ie a country check of the Current Store Front
	 * 
	 * @param catalogueUuid
	 * @return
	 */
	@ApiOperation(value = "Get information on a catalogue")
	@GET
	@Path("/{uuid}")
	public StoreCatalogueBean getCatalogue(@ApiParam(value = "Catalogue UUID") @PathParam("uuid") String catalogueUuid)
	{
		Catalogue catalogue = verifyCatalogueForStore(getStoreFront(), catalogueUuid);
		return convertCatalogueToBean(catalogue);
	}

	// search (similar to standard search, except a catalogue UUID is REQUIRED)
	@ApiOperation(value = "Search for items")
	@GET
	@Path("/{uuid}/search")
	// @formatter:off
	public StoreCatalogueSearchBean searchItems(
		@ApiParam(value="Catalogue UUID", required = true) 
		@PathParam("uuid") 
			String catalogueUuid,
		@ApiParam(value="Query string", required = false) 
		@QueryParam("q") 
			String q,
		@ApiParam(value="The first record of the search results to return", required = false, defaultValue="0") 
		@QueryParam("start") 
			int start,
		@ApiParam(value="The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,100]") 
		@QueryParam("length") 
			int length,
		@ApiParam(value="The order of the search results", allowableValues="relevance,modified,name", required = false) 
		@QueryParam("order")
			String order,
		@ApiParam(value="The date filter to use", allowableValues="between, before, after, on", required = false) 
		@QueryParam("datefilter")
			String datefilter,
		@ApiParam(value="The date start, to be used for all date filters", required = false) 
		@QueryParam("datestart")
			String datestart,
		@ApiParam(value="The date end, only to be used if datefilter=between", required = false) 
		@QueryParam("dateend")
			String dateend,
		@ApiParam(value="The price filter to use", allowableValues="all, purchase, free, subscription", required = false) 
		@QueryParam("pricefilter")
			String priceFilter,
		@ApiParam(value="Reverse the order of the search results", allowableValues = "true,false", defaultValue = "false", required = false) 
		@QueryParam("reverse")
			String reverse
		)
	// @formatter:on
	{
		final StoreFront storeFront = getStoreFront();
		verifyCatalogueForStore(storeFront, catalogueUuid);

		final PaymentSettings settings = getPaymentSettings();
		final DefaultSearch searchReq = catService.createLiveSearch(catalogueUuid, storeFront);
		searchReq.setPrivilege(null);

		final StoreCatalogueSearchBean result = new StoreCatalogueSearchBean();

		if( !Check.isEmpty(q) )
		{
			searchReq.setQuery(q);
		}

		int unfilteredResults = freetextService.countsFromFilters(Collections.singleton(searchReq))[0];

		if( !Check.isEmpty(datefilter) )
		{
			searchReq.setDateRange(getDateRange(datefilter, datestart, dateend));
		}

		List<BaseEntityLabel> purchaseTiers = pricingTierService.listAll(true);
		List<String> purchaseTierIds = new ArrayList<String>();
		for( BaseEntityLabel b : purchaseTiers )
		{
			purchaseTierIds.add(b.getUuid());
		}

		List<BaseEntityLabel> subscriptionTiers = pricingTierService.listAll(false);
		List<String> subscriptionTierIds = new ArrayList<String>();
		for( BaseEntityLabel b : subscriptionTiers )
		{
			subscriptionTierIds.add(b.getUuid());
		}

		final boolean reverseOrder = (reverse != null && Utils.parseLooseBool(reverse, false));
		SortField primarySort = getSortField(order, q, reverseOrder);

		// Refactor this with transforms if you want, this is easier
		if( !Check.isEmpty(priceFilter) )
		{
			if( priceFilter.equals("purchase") )
			{
				searchReq.addMust(PaymentIndexFields.FIELD_PURCHASE_TIER, purchaseTierIds);
			}
			else
			{
				// sort field of price is not valid for non-purchase
				// if (primarySort is price)
				// {
				// primarySort = SortType.DATEMODIFIED.getSortField();
				// }

				if( priceFilter.equals("free") )
				{
					searchReq.addMust(PaymentIndexFields.FIELD_FREE_TIER, "true");
				}
				else if( priceFilter.equals("subscription") )
				{
					searchReq.addMust(PaymentIndexFields.FIELD_SUBSCRIPTION_TIER, subscriptionTierIds);
				}
			}
		}
		searchReq.setSortFields(primarySort, SortType.DATEMODIFIED.getSortField(reverseOrder));

		final int offset = (start < 0 ? 0 : start);
		final int count = (length <= 0 ? 10 : length);

		final SearchResults<ItemIdKey> searchResults = freetextService.searchIds(searchReq, offset, count);
		final List<ItemIdKey> itemIdKeys = searchResults.getResults();

		final List<Long> itemIds = Lists.transform(itemIdKeys, new Function<ItemIdKey, Long>()
		{
			@Override
			public Long apply(ItemIdKey input)
			{
				return input.getKey();
			}
		});

		final List<StoreCatalogueItemBean> resultItems = Lists.newArrayList();

		final ItemSerializerItemBean itemSerializer = itemSerializerService.createItemBeanSerializer(itemIds, Lists
			.newArrayList(ItemSerializerService.CATEGORY_BASIC, ItemSerializerService.CATEGORY_ATTACHMENT,
				ItemSerializerService.CATEGORY_DETAIL), true);
		for( ItemIdKey itemId : itemIdKeys )
		{
			final PricingTierAssignment pta = tierService.getPricingTierAssignmentForItem(itemId);
			final boolean free = (pta == null ? false : pta.isFreeItem() && storeFront.isAllowFree()
				&& settings.isFreeEnabled());
			final StoreCatalogueItemBean storeItemBean = loadCatalogueItemBean(itemSerializer, catalogueUuid, itemId,
				free, getPurchaseTier(pta, settings, storeFront), getSubscriptionTier(pta, settings, storeFront),
				getTaxes(storeFront), false);
			resultItems.add(storeItemBean);
		}

		result.setStart(searchResults.getOffset());
		result.setLength(searchResults.getCount());
		result.setAvailable(searchResults.getAvailable());
		result.setResults(resultItems);
		result.setFiltered(unfilteredResults - searchResults.getAvailable());
		return result;
	}

	private List<TaxType> getTaxes(StoreFront storeFront)
	{
		TaxType taxType = storeFront.getTaxType();
		if( taxType != null )
		{
			return Lists.newArrayList(taxType);
		}
		return Collections.emptyList();
	}

	private Date[] getDateRange(String datefilter, String datestart, String dateend) throws NullPointerException,
		NumberFormatException
	{
		Date[] dateRange = new Date[2];
		if( datefilter.equals("between") )
		{
			dateRange[0] = ISO8601Utils.parse(datestart);
			dateRange[1] = ISO8601Utils.parse(dateend);
		}
		else if( datefilter.equals("before") )
		{
			dateRange[0] = null;
			dateRange[1] = ISO8601Utils.parse(datestart);
		}
		else if( datefilter.equals("after") )
		{
			dateRange[0] = ISO8601Utils.parse(datestart);
			dateRange[1] = null;
		}
		else if( datefilter.equals("on") )
		{
			dateRange[0] = ISO8601Utils.parse(datestart);
			dateRange[1] = new Date(ISO8601Utils.parse(datestart).getTime() + TimeUnit.DAYS.toMillis(1));
		}
		return dateRange;
	}

	@ApiOperation(value = "Get a catalogue item")
	@GET
	@Path("/{uuid}/item/{itemUuid}")
	public StoreCatalogueItemBean getCatalogueItem(
		@ApiParam(value = "Catalogue UUID", required = true) @PathParam("uuid") String catalogueUuid,
		@ApiParam(value = "Item UUID", required = true) @PathParam("itemUuid") String itemUuid)
	{
		final StoreFront storeFront = getStoreFront();
		verifyCatalogueForStore(storeFront, catalogueUuid);

		final ItemIdKey itemId = itemService.getLiveItemVersionId(itemUuid);

		// ensure it's in the catalogue
		if( itemId == null || !catService.containsLiveItem(catalogueUuid, itemId, storeFront) )
		{
			throw new NotFoundException(resources.getString(KEY_ERROR_NOT_IN_CATALOGUE));
		}
		// final Item item = itemService.getUnsecure(itemId);

		final PricingTierAssignment pta = tierService.getPricingTierAssignmentForItem(itemId);

		final PaymentSettings settings = getPaymentSettings();
		final boolean free = (pta == null ? false : pta.isFreeItem() && storeFront.isAllowFree()
			&& settings.isFreeEnabled());

		final StoreCatalogueItemBean s = loadCatalogueItemBean(catalogueUuid, itemId, free,
			getPurchaseTier(pta, settings, storeFront), getSubscriptionTier(pta, settings, storeFront),
			getTaxes(storeFront));
		return s;
	}

	private PricingTier getPurchaseTier(PricingTierAssignment pta, PaymentSettings settings, StoreFront storeFront)
	{
		final PricingTier pt = (pta == null ? null : (storeFront.isAllowPurchase() && settings.isPurchaseEnabled()
			? pta.getPurchasePricingTier() : null));
		if( pt != null && pt.isDisabled() )
		{
			return null;
		}
		return pt;
	}

	private PricingTier getSubscriptionTier(PricingTierAssignment pta, PaymentSettings settings, StoreFront storeFront)
	{
		final PricingTier st = (pta == null ? null : (storeFront.isAllowSubscription()
			&& settings.isSubscriptionEnabled() ? pta.getSubscriptionPricingTier() : null));
		if( st != null && st.isDisabled() )
		{
			return null;
		}
		return st;
	}

	/**
	 * You need to stream this from your store to you customer, it won't bypass
	 * security. See shopViewItemSection for an example.<br>
	 * Tests for existence and value of attachment.preview flag, and will either
	 * fetch attachment content where flag exists and is true, or 'access
	 * denied' otherwise.
	 * 
	 * @param catalogueUuid
	 * @param itemUuid
	 * @param attachUuid
	 * @return
	 */
	@ApiOperation(value = "Get an attachment preview")
	@GET
	@Path("/{uuid}/item/{itemUuid}/attachment/{attachUuid}")
	public Response getPreviewAttachment(
		@ApiParam(value = "Catalogue UUID", required = true) @PathParam("uuid") String catalogueUuid,
		@ApiParam(value = "Item UUID", required = true) @PathParam("itemUuid") String itemUuid,
		@ApiParam(value = "Attachment UUID", required = true) @PathParam("attachUuid") String attachUuid,
		@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		final int liveVersion = itemService.getLiveItemVersion(itemUuid);
		final ItemId itemId = new ItemId(itemUuid, liveVersion);
		final String catId = catalogueUuid;

		boolean found = catService.containsLiveItem(catId, itemId, getStoreFront());
		if( !found )
		{
			throw new NotFoundException(resources.getString(KEY_ERROR_NOT_IN_CATALOGUE));
		}

		Attachment attachment = null;
		// Irrespective of payment status, if the item + attachment as requested
		// doesn't exist, throw an exception to that effect
		try
		{
			// a search with both uuids, so fail if either invalid
			attachment = itemService.getAttachmentForUuid(itemId, attachUuid);
		}
		catch( AttachmentNotFoundException anfe )
		{
			throw new NotFoundException(anfe.getMessage());
		}

		// We can download this attachment iff it has been flagged by its
		// owner/editor as a preview ...
		if( attachment.isPreview() )
		{
			return serveContentStreamResponse(new ItemFile(itemUuid, liveVersion), attachment.getUrl(), request,
				response);
		}
		// ... requested attachment is not flagged as a preview; forbidden here
		return Response.status(Status.NOT_FOUND).build();
	}

	private StoreCatalogueBean convertCatalogueToBean(Catalogue catalogue)
	{
		final StoreCatalogueBean bean = serializer.convertCatalogueToBean(catalogue);
		final Map<String, String> linkMap = Collections.singletonMap("self", getCatalogueSelfLink(catalogue.getUuid())
			.toString());
		bean.set("links", linkMap);

		return bean;
	}

	private StoreCatalogueItemBean loadCatalogueItemBean(String catUuid, ItemIdKey itemId, boolean free,
		PricingTier purchaseTier, PricingTier subscriptionTier, List<TaxType> taxes)
	{
		final ItemSerializerItemBean itemSerializer = itemSerializerService.createItemBeanSerializer(Collections
			.singletonList(itemId.getKey()), Lists.newArrayList(ItemSerializerService.CATEGORY_BASIC,
			ItemSerializerService.CATEGORY_ATTACHMENT, ItemSerializerService.CATEGORY_DETAIL, "navigation"), true);
		return loadCatalogueItemBean(itemSerializer, catUuid, itemId, free, purchaseTier, subscriptionTier, taxes, true);
	}

	private StoreCatalogueItemBean loadCatalogueItemBean(ItemSerializerItemBean itemSerializer, String catUuid,
		ItemIdKey itemId, boolean free, PricingTier purchaseTier, PricingTier subscriptionTier, List<TaxType> taxes,
		boolean allLinks)
	{
		final EquellaItemBean equellaItemBean = new EquellaItemBean();
		itemSerializer.writeItemBeanResult(equellaItemBean, itemId.getKey());
		// Why the hell doesn't the serializer do this???
		equellaItemBean.setUuid(itemId.getUuid());
		equellaItemBean.setVersion(itemId.getVersion());

		final StoreCatalogueItemBean bean = serializer.convertItemBeanToCatalogueItemBean(equellaItemBean, catUuid,
			free, purchaseTier, subscriptionTier, new TaxCalculator(taxService, taxes), taxes);
		final Map<String, String> linkMap = Collections.singletonMap("self",
			getCatalogueItemSelfLink(catUuid, itemId.getUuid()).toString());
		bean.set("links", linkMap);
		storeBeanLinkService.addLinks(bean, catUuid, !allLinks);
		return bean;
	}

	private URI getCatalogueSelfLink(String catalogueUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getCatalogue").build(catalogueUuid);
	}

	private URI getCatalogueItemSelfLink(String catalogueUuid, String itemUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getCatalogueItem").build(catalogueUuid, itemUuid);
	}

	private SortField getSortField(String order, String q, boolean reverse)
	{
		if( order != null )
		{
			// allowed values are relevance, modified, name
			if( order.equals("relevance") )
			{
				if( Check.isEmpty(q) )
				{
					return SortType.DATEMODIFIED.getSortField(reverse);
				}
				return SortType.RANK.getSortField(reverse);
			}
			else if( order.equals("modified") )
			{
				return SortType.DATEMODIFIED.getSortField(reverse);
			}
			else if( order.equals("name") )
			{
				return SortType.NAME.getSortField(reverse);
			}
			// Not currently supported
			/*
			 * else if( order.equals("price") ) { return new SortField("",
			 * reverse); }
			 */
		}

		// default is 'modified' for a blank query and 'relevance' for anything
		// else (as is the case for the UI)
		if( Check.isEmpty(q) )
		{
			return SortType.DATEMODIFIED.getSortField(reverse);
		}
		return SortType.RANK.getSortField(reverse);
	}

	private PaymentSettings getPaymentSettings()
	{
		return configService.getProperties(new PaymentSettings());
	}

}
