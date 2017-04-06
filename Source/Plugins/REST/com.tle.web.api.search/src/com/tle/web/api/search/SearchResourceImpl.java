package com.tle.web.api.search;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.interfaces.CsvList;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.DateHelper;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.remoting.MatrixResults.MatrixEntry;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.services.item.FreeTextService;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.search.interfaces.beans.FacetBean;
import com.tle.web.api.search.interfaces.beans.FacetSearchBean;

/**
 * @author Aaron & Dustin
 */
@SuppressWarnings("nls")
@Bind(EquellaSearchResource.class)
@Singleton
public class SearchResourceImpl implements EquellaSearchResource
{
	private static final Logger LOGGER = Logger.getLogger(SearchResourceImpl.class);

	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private DynaCollectionService dynaCollectionService;

	@Override
	public SearchBean<ItemBean> searchItems(UriInfo uriInfo, String q, int start, int length, CsvList collections,
		String order, String reverse, String where, CsvList info, String showall)
	{
		// EQUELLA uses method below with additional param
		return searchItems(uriInfo, q, start, length, collections, order, reverse, where, info, showall, null);
	}

	@Override
	public SearchBean<ItemBean> searchItems(UriInfo uriInfo, String q, int start, int length, CsvList collections,
		String order, String reverse, String where, CsvList info, String showall, String dynaCollectionCompound)
	{
		Preconditions.checkArgument(length <= 50, "Length must be less than or equal to 50");

		final SearchBean<ItemBean> result = new SearchBean<ItemBean>();
		final List<ItemBean> resultItems = Lists.newArrayList();

		// sanitise parameters
		final String whereClause = where;
		final boolean onlyLive = !(showall != null && Utils.parseLooseBool(showall, false));
		final SortType orderType = DefaultSearch.getOrderType(order, q);
		final boolean reverseOrder = (reverse != null && Utils.parseLooseBool(reverse, false));
		final int offset = (start < 0 ? 0 : start);
		final int count = (length < 0 ? 10 : length);
		final List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_BASIC);

		// String dynaCollectionCompound =
		// uriInfo.getQueryParameters().getFirst("dynacollection");
		DefaultSearch search = createSearch(q, collections == null ? null : CsvList.asList(collections), whereClause,
			onlyLive, orderType, reverseOrder, null, dynaCollectionCompound, new DefaultSearch());

		final SearchResults<ItemIdKey> searchResults = freetextService.searchIds(search, offset, count);

		final List<ItemIdKey> itemIds = searchResults.getResults();
		final List<Long> ids = Lists.transform(itemIds, new Function<ItemIdKey, Long>()
		{
			@Override
			public Long apply(ItemIdKey input)
			{
				return input.getKey();
			}
		});

		final ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(ids, infos);
		for( ItemIdKey itemId : itemIds )
		{
			EquellaItemBean itemBean = new EquellaItemBean();
			itemBean.setUuid(itemId.getUuid());
			itemBean.setVersion(itemId.getVersion());
			try
			{
				serializer.writeItemBeanResult(itemBean, itemId.getKey());
				itemLinkService.addLinks(itemBean);
			}
			catch( NotFoundException nfe )
			{
				LOGGER.warn("Item in search results not found, possibly purged.", nfe);
			}

			resultItems.add(itemBean);
		}

		result.setStart(searchResults.getOffset());
		result.setLength(searchResults.getCount());
		result.setAvailable(searchResults.getAvailable());
		result.setResults(resultItems);
		return result;
	}

	@Override
	public FacetSearchBean searchFacets(CsvList nodes, String nestLevel, String q, int breadth, CsvList collections,
		String where, String showall)
	{
		final String whereClause = where;
		final boolean onlyLive = !(showall != null && Utils.parseLooseBool(showall, false));
		final Collection<String> cols = (collections == null ? null : CsvList.asList(collections));
		final int width = (breadth < 0 ? 10 : breadth);
		final List<String> nodeList = CsvList.asList(nodes);

		final DefaultSearch search = createSearch(q, cols, whereClause, onlyLive, SortType.RANK, false, null, null,
			new DefaultSearch());

		final MatrixResults matrixResults = freetextService.matrixSearch(search, nodeList, true, width);

		final FacetSearchBean resultsBean = new FacetSearchBean();
		final List<FacetBean> facetBeans = Lists.newArrayList();
		for( MatrixEntry matrixEntry : matrixResults.getEntries() )
		{
			final FacetBean facet = new FacetBean();
			facet.setCount(matrixEntry.getCount());
			facet.setTerm(Utils.join(matrixEntry.getFieldValues().toArray(), ","));
			facetBeans.add(facet);
		}
		resultsBean.setResults(facetBeans);

		return resultsBean;
	}

	private DefaultSearch createSearch(String freetext, Collection<String> collectionUuids, String whereClause,
		boolean onlyLive, SortType sortType, boolean reverseOrder, String modifiedSince, String dynaCollectionCompound,
		DefaultSearch search)
	{
		FreeTextBooleanQuery freetextQuery = null;
		if( !Check.isEmpty(dynaCollectionCompound) )
		{
			VirtualisableAndValue<DynaCollection> virtiualDynaColl = dynaCollectionService
				.getByCompoundId(dynaCollectionCompound);

			if( virtiualDynaColl == null )
			{
				throw new RuntimeException(dynaCollectionCompound + ": not a valid dynamic collection uuid");
			}
			DynaCollection dynaCollection = virtiualDynaColl.getVt();
			// this will check if the dynamic collection has a virtualization
			// path, because
			// there's no future in passing a virtualization value if the
			// dynamic collection
			// is not virtualized, nor vice versa (omitting virtual value if it
			// is)
			String[] deconstrunctedCompound = dynaCollectionCompound.split(":");
			String virtual = deconstrunctedCompound.length > 1 ? deconstrunctedCompound[1] : null;
			freetextQuery = dynaCollectionService.getSearchClause(dynaCollection, virtual);
		}
		// Collection uuids are whatever we asked for ...
		search.setCollectionUuids(collectionUuids);
		// ... and the where clause if present, is engaged.
		if( !Check.isEmpty(whereClause) )
		{
			FreeTextBooleanQuery whereQuery = WhereParser.parse(whereClause);
			if( freetextQuery != null )
			{
				freetextQuery.add(whereQuery);
			}
			else
			{
				freetextQuery = whereQuery;
			}
		}

		// set it, null or not
		search.setFreeTextQuery(freetextQuery);

		if( onlyLive )
		{
			search.setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}
		search.setSortFields(sortType.getSortField(reverseOrder));
		search.setQuery(freetext);

		if( !Check.isEmpty(modifiedSince) )
		{
			// We really only expect Date, but if they append as an ISO
			// DateTime, we'll forgive
			Date sinceDate = null;
			try
			{
				UtcDate utcDate = UtcDate.conceptualDate(modifiedSince);
				sinceDate = utcDate.toDate();
			}
			catch( Exception e )
			{
				// continue - sinceDate will be null
			}
			if( sinceDate == null )
			{
				// second and last chance
				sinceDate = DateHelper.parseOrNullDate(modifiedSince, Dates.ISO);
				if( sinceDate == null )
				{
					throw new RuntimeException("not in ISO (yyyy-mm-dd) format: (" + modifiedSince + ')');
				}
			}
			search.setDateRange(new Date[]{sinceDate, new Date()});
		}

		return search;
	}
}
