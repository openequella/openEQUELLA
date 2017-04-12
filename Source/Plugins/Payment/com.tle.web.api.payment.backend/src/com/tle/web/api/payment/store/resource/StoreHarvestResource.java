package com.tle.web.api.payment.store.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.time.DateUtils;

import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.WebException;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemsBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.dao.SaleItemDao;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.ItemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Harvestable item endpoint. Definition of "paid for":<br>
 * There is at least one existing transaction for the associated store front of
 * the user making the call with a subscription date that is in range.<br>
 * OR<br>
 * There is at least one existing free or purchase transaction<br>
 * Note for the purposes of downloading an item the subscription period starts a
 * day before the actual start date (it needs to be harvestable before the
 * subscription period actually starts)
 * 
 * @author Aaron, so it must be good. Pity t'was co-authored by Larry
 */
@Bind
@Singleton
@Path("store/harvest")
@Api(value = "/store/harvest", description = "store-harvest")
@Produces("application/json")
@SuppressWarnings("nls")
public class StoreHarvestResource extends AbstractStoreResource
{
	public static final String HARVEST_INFOTERM = "harvest";

	@Inject
	private SaleItemDao saleItemDao;
	@Inject
	private PaymentService paymentService;
	@Inject
	private ItemService itemService;
	@Inject
	private StoreBeanSerializer serializer;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private MimeTypeService mimeService;

	/**
	 * @param updatedSinceDate an ISO date. Narrows the list to bought items
	 *            that have been updated since this date
	 * @param onlyNew Narrows the list to items that have never been downloaded
	 * @param info comma separated list of info. only "harvest" is supported
	 *            (but we may think of more if needed)
	 * @param start
	 * @param length
	 * @return
	 */
	@GET
	@Path("")
	@ApiOperation(value = "Retrieve a list of all paid-for resources")
	// @formatter:off
	public StoreHarvestableItemsBean getAllPaidForItems(	
		@ApiParam(value = "Created since date", required = false)
		@QueryParam("createdSince")
			String createdSinceDate,
		@ApiParam(value = "only new", required = false, defaultValue = "false", allowableValues = "true,false")
		@QueryParam("new")
			String onlyNew,
		@ApiParam(value = "comma separated list of info required", required = false)
		@QueryParam("info")
			String info,
		@ApiParam(value = "The first record of the search results to return", required = false, defaultValue = "0")
		@QueryParam("start")
			int start,
		@ApiParam(value = "The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,100]")
		@QueryParam("length")
			int length)
	// @formatter:on
	{
		final StoreFront storeFront = getStoreFront();

		start = (start < 0 ? 0 : start);
		length = (length <= 0 ? 10 : length);
		boolean onlyNewFlag = (onlyNew != null && Utils.parseLooseBool(onlyNew, false));

		Date createdSince = dateFromArgString(createdSinceDate);

		String[] infoTermsStrs = Check.isEmpty(info) ? null : info.split(",");
		List<String> infoTerms = (infoTermsStrs != null ? Arrays.asList(infoTermsStrs) : new ArrayList<String>());

		Calendar today = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);

		int available = saleItemDao.countPaidForSaleItems(storeFront, today.getTime(), onlyNewFlag, createdSince);

		StoreHarvestableItemsBean harvestableItemsBean = new StoreHarvestableItemsBean();

		if( available > 0 )
		{
			List<SaleItem> saleItems = saleItemDao.getPaidForSaleItems(storeFront, today.getTime(), onlyNewFlag,
				createdSince, start, length);
			List<StoreHarvestableItemBean> results = addSaleItemDataToResultSet(saleItems, infoTerms);
			harvestableItemsBean.setResults(results);
		}
		else
		{
			harvestableItemsBean.setResults(Collections.<StoreHarvestableItemBean> emptyList());
		}

		harvestableItemsBean.setStart(start);
		harvestableItemsBean.setLength(harvestableItemsBean.getResults().size());
		harvestableItemsBean.setAvailable(available);

		return harvestableItemsBean;
	}

	/**
	 * @param uuid
	 * @return if item is paid for; name, description, metadata, links to
	 *         attachment download URLs (
	 *         api/store/harvest/item/[uuid]/[attachuuid] )<br>
	 *         otherwise "Payment required"
	 */
	@GET
	@Path("/item/{uuid}")
	@ApiOperation(value = "Retrieve a harvestable item")
	public StoreHarvestableItemBean getItem(@PathParam("uuid") String uuid)
	{
		final StoreFront storeFront = getStoreFront();

		StoreHarvestableItemBean storeItemBean;

		// is there a saleItem for this EQUELLA item (true = must be paid for)
		List<SaleItem> saleItems = saleItemDao.findSaleItemByItemUuid(storeFront, uuid, true);
		if( !Check.isEmpty(saleItems) )
		{
			filterOutOfRangeSubscriptions(saleItems);
		}

		if( saleItems.size() > 0 )
		{
			SaleItem theSaleItem = getLatestSaleItem(saleItems);

			String itemUuid = theSaleItem.getItemUuid();
			final Item item = itemService.getLatestVersionOfItem(itemUuid);

			storeItemBean = serializer.convertItemToHarvestableItemBean(item);
		}
		else
		{
			// 402 - a HTTP code whose time has come 'Payment required' but
			// asterixed as 'reserved for future use'
			throw new WebException(402, "payment_required", resources.getString("error.payment.required"));
		}

		paymentService.recordHarvest(storeItemBean.getUuid(), storeItemBean.getVersion(), saleItems.get(0).getSale());

		addStoreAttachmentLinks(storeItemBean);

		return storeItemBean;
	}

	private void addStoreAttachmentLinks(StoreHarvestableItemBean storeItemBean)
	{
		for( AttachmentBean attachment : storeItemBean.getAttachments() )
		{
			String link = urlLinkService.getMethodUriBuilder(getClass(), "downloadItemAttachment")
				.build(storeItemBean.getUuid(), attachment.getUuid()).toString();

			final Map<String, String> attachLinks = Maps.newHashMap();
			attachLinks.put("harvest", link);
			attachment.set("links", attachLinks);
		}
	}

	private SaleItem getLatestSaleItem(List<SaleItem> saleItems)
	{
		if( saleItems == null || saleItems.isEmpty() )
		{
			return null;
		}

		if( saleItems.size() == 1 )
		{
			return saleItems.get(0);
		}

		ArrayList<SaleItem> items = Lists.newArrayList(saleItems);
		Collections.sort(items, new Comparator<SaleItem>()
		{
			@Override
			public int compare(SaleItem s1, SaleItem s2)
			{
				// purchases/free come first
				Date end1 = s1.getSubscriptionEndDate();
				Date end2 = s2.getSubscriptionEndDate();
				if( end1 == null )
				{
					if( end2 != null )
					{
						return -1;
					}
					else
					// both null
					{
						return 0;
					}
				}
				else
				// end1 not null
				{
					if( end2 == null )
					{
						return 1;
					}
					else
					// both are subscription
					{
						return end1.compareTo(end2);
					}
				}
			}
		});

		return items.get(0);
	}

	/**
	 * @param uuid String item uuid
	 * @param attachuuid String attachment uuid
	 * @return if item is paid for; the contents of the attachment<br>
	 *         otherwise "Payment required"
	 */
	@GET
	@Produces(MediaType.WILDCARD)
	@Path("/item/{uuid}/content")
	@ApiOperation(value = "Download a zip of the resource's files")
	public Response downloadItemFiles(@PathParam("uuid") String uuid)
	{
		final StoreFront storeFront = getStoreFront();

		// is there a saleItem for this EQUELLA item (true = must be paid for)
		List<SaleItem> saleItems = saleItemDao.findSaleItemByItemUuid(storeFront, uuid, true);
		if( !Check.isEmpty(saleItems) )
		{
			filterOutOfRangeSubscriptions(saleItems);
		}

		if( saleItems.size() > 0 )
		{
			Item item = itemService.getLatestVersionOfItem(uuid);
			if( item == null )
			{
				throw new WebApplicationException(Status.NOT_FOUND);
			}
			final ItemFile itemFile = new ItemFile(item.getUuid(), item.getVersion());
			Response response = Response.ok().type("application/x-gtar").entity(new StreamingOutput()
			{
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException
				{
					fileSystemService.zipFile(itemFile, output, ArchiveType.TAR_GZ);
				}
			}).build();

			paymentService.recordHarvest(uuid, item.getVersion(), "*", saleItems.get(0).getSale());
			return response;
		}

		// 402 - a HTTP code whose time has come 'Payment required' but
		// asterixed as 'reserved for future use'
		throw new WebException(402, "payment_required", resources.getString("error.payment.required"));
	}

	/**
	 * @param uuid String item uuid
	 * @param attachuuid String attachment uuid
	 * @return if item is paid for; the contents of the attachment<br>
	 *         otherwise "Payment required"
	 */
	@GET
	@Path("/item/{uuid}/{attachuuid}")
	@ApiOperation(value = "Download a single attachment on a resource")
	public Response downloadItemAttachment(@PathParam("uuid") String uuid, @PathParam("attachuuid") String attachuuid)
	{
		if( attachuuid.equals("content") )
		{
			return downloadItemFiles(uuid);
		}

		final StoreFront storeFront = getStoreFront();

		Attachment attachment = null;
		// We don't require caller to pass item version that was paid for
		int liveVersion = 1;
		// Irrespective of payment status, if the item + attachment as requested
		// doesn't exist, throw an exception to that effect
		try
		{
			liveVersion = itemService.getLiveItemVersion(uuid);
			// a search with both uuids, so fail if either invalid
			attachment = itemService.getAttachmentForUuid(new ItemId(uuid, liveVersion), attachuuid);
		}
		catch( AttachmentNotFoundException anfe )
		{
			throw new WebException(400, "item_not_found", anfe.getMessage());
		}

		// is there a saleItem for this EQUELLA item (true = must be paid for)
		List<SaleItem> saleItems = saleItemDao.findSaleItemByItemUuid(storeFront, uuid, true);
		if( !Check.isEmpty(saleItems) )
		{
			filterOutOfRangeSubscriptions(saleItems);
		}

		if( saleItems.size() > 0 )
		{
			SaleItem theSaleItem = getLatestSaleItem(saleItems);

			final FileHandle handle = new ItemFile(uuid, liveVersion);
			final String filename = attachment.getUrl();
			ensureFileExists(handle, filename);
			final String mimeType = mimeService.getMimeTypeForFilename(filename);
			Response streamResponse = Response.ok().type(mimeType).entity(new StreamingOutput()
			{
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException
				{
					try (InputStream input = fileSystemService.read(handle, filename))
					{
						ByteStreams.copy(input, output);
					}
				}
			}).build();

			paymentService.recordHarvest(uuid, liveVersion, attachuuid, theSaleItem.getSale());
			return streamResponse;
		}

		// 402 - a HTTP code whose time has come 'Payment required' but
		// asterixed as 'reserved for future use'
		throw new WebException(402, "payment_required", resources.getString("error.payment.required"));
	}

	/**
	 * Get a list of items expiring within a date. Only subscribed resources are
	 * returned. This endpoint tells a store front what it should be keeping
	 * active and what it should be expiring.<br>
	 * When expired = false (the default), the caller is asking: what subscribed
	 * material will be available at any point within the from...until period?
	 * This will include material which becomes available during the period, and
	 * material which is available initially but ceases to become available by
	 * the end of the period (as well as material which has an available span
	 * wholly within the period).<br>
	 * When expired = true, the caller is asking: what subscribed material will
	 * cease to be available within the from...until period? The purpose of such
	 * a query would be to tell the caller what material they need to think
	 * about retrieving or renewing.<br>
	 * E.g. to find the resources expiring in the next week:<br>
	 * GET api/store/harvest/subscription?expired=true&from=â€�nowâ€�&until=
	 * "now + 7 days"<br>
	 * To find the active resources within an arbitrary period:<br>
	 * GET api/store/harvest/subscription?from=X&until=Y
	 * 
	 * @param expired Changes the behaviour of 'from' and 'until', ie. expired
	 *            vs active. (false if not specified)
	 * @param from ISO date. Mandatory. resources active/expired after this date
	 * @param until ISO date. Mandatory. resources active/expired before this
	 *            date
	 * @param info comma separated list of info. only "harvest" is supported
	 *            (but we may think of more if needed)
	 * @param start = first result index
	 * @param length = number of results
	 * @return List of simplified items, with subscription info (ie. when it
	 *         starts, when it expires). also with harvest history information
	 *         if info=harvest
	 */
	@GET
	@Path("/subscription")
	@ApiOperation(value = "Return a list of resources that are current or expiring")
	// @formatter:off
	public StoreHarvestableItemsBean getSubscribedSaleItemsExpiring(
		@ApiParam(value = "expired", required = false, defaultValue = "false", allowableValues = "true,false")
		@QueryParam("expired")
			String expired,
		@ApiParam(value = "from date", required = true)
		@QueryParam("from")
			String from,
		@ApiParam(value = "until date", required = true)
		@QueryParam("until")
			String until,
		@ApiParam(value = "comma separated list of info required", required = false)
		@QueryParam("info")
			String info,
		@ApiParam(value = "The first record of the search results to return", required = false, defaultValue = "0")
		@QueryParam("start")
			int start,
		@ApiParam(value = "The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,100]")
		@QueryParam("length")
			int length)
	// @formatter:on
	{
		final StoreFront storeFront = getStoreFront();

		start = (start < 0 ? 0 : start);
		length = (length <= 0 ? 10 : length);
		// the default is false, which is to say, retrieve subscriptions active
		// within period
		boolean expiredFlag = (expired != null && Utils.parseLooseBool(expired, false));

		String[] infoTermsStrs = Check.isEmpty(info) ? null : info.split(",");
		List<String> infoTerms = (infoTermsStrs != null ? Arrays.asList(infoTermsStrs) : new ArrayList<String>());

		// from and until dates are mandatory - something more meaningful than a
		// NullPointerException
		if( Check.isEmpty(from) || Check.isEmpty(until) )
		{
			throw new WebException(400, "client_error", resources.getString("both.dates.required"));
		}
		Date startOfPeriod = dateFromArgString(from);
		Date endOfPeriod = dateFromArgString(until);

		int available = 0;
		List<SaleItem> saleItems = null;

		if( expiredFlag )
		{
			available = saleItemDao.countSubscriptionsExpiringWithinPeriod(storeFront, startOfPeriod, endOfPeriod);
		}
		else
		{
			available = saleItemDao.countSubscriptionsActiveWithinPeriod(storeFront, startOfPeriod, endOfPeriod);
		}

		// If the first count-available query fetches 0, then there's nothing to
		// be expected from a retrieval
		if( available > 0 )
		{
			if( expiredFlag )
			{
				saleItems = saleItemDao.getSubscriptionsExpiringWithinPeriod(storeFront, startOfPeriod, endOfPeriod,
					start, length);
			}
			else
			{
				saleItems = saleItemDao.getSubscriptionsActiveWithinPeriod(storeFront, startOfPeriod, endOfPeriod,
					start, length);
			}
		}
		else
		// empty result set
		{
			saleItems = new ArrayList<SaleItem>();
		}

		List<StoreHarvestableItemBean> results = addSaleItemDataToResultSet(saleItems, infoTerms);

		StoreHarvestableItemsBean harvestableItemsBean = new StoreHarvestableItemsBean();
		harvestableItemsBean.setResults(results);
		harvestableItemsBean.setStart(start);
		harvestableItemsBean.setLength(results.size());
		harvestableItemsBean.setAvailable(available);

		return harvestableItemsBean;
	}

	/**
	 * From a List of SaleItem, remove those which are Subscriptions, where<br>
	 * subscription start date is in the future, or subscription end date is in
	 * the past
	 */
	private List<SaleItem> filterOutOfRangeSubscriptions(List<SaleItem> saleItems)
	{
		for( Iterator<SaleItem> iter = saleItems.iterator(); iter.hasNext(); )
		{
			SaleItem saleItem = iter.next();
			Date subscriptionStartDate = saleItem.getSubscriptionStartDate();
			if( subscriptionStartDate != null )
			{
				subscriptionStartDate = DateUtils.truncate(subscriptionStartDate, Calendar.DATE);
				Date subscriptionEndDate = DateUtils.ceiling(saleItem.getSubscriptionEndDate(), Calendar.DATE);

				Date now = new Date();
				if( now.before(subscriptionStartDate) || now.after(subscriptionEndDate) )
				{
					// out of range: remove from result List
					iter.remove();
				}
				// else it's either a purchase or free; either way it stays in
				// the list
			}
		}
		return saleItems;
	}

	private List<StoreHarvestableItemBean> addSaleItemDataToResultSet(List<SaleItem> saleItems, List<String> infoTerms)
	{
		List<StoreHarvestableItemBean> results = new ArrayList<StoreHarvestableItemBean>();
		for( SaleItem saleItem : saleItems )
		{
			StoreHarvestableItemBean harvestableItemBean = serializer
				.convertSaleItemToStoreHarvestableItemBean(saleItem);

			if( infoTerms.contains(HARVEST_INFOTERM) )
			{
				List<StoreHarvestInfo> harvestHistory = paymentService.getHarvestHistoryForSaleItem(saleItem.getUuid());
				harvestableItemBean.setHarvestInfos(harvestHistory);
			}
			final Map<String, String> linkMap = Collections.singletonMap("self",
				getSaleItemSelfLink(saleItem.getUuid()).toString());
			harvestableItemBean.set("links", linkMap);

			results.add(harvestableItemBean);
		}
		return results;
	}

	private URI getSaleItemSelfLink(String saleItemUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getItem").build(saleItemUuid);
	}

	private Date dateFromArgString(String argString)
	{
		try
		{
			return Check.isEmpty(argString) ? null : ISO8601Utils.parse(argString);
		}
		catch( Exception poe )
		{
			throw new WebException(400, "unreadable_date", poe.getMessage());
		}
	}
}
