package com.tle.searching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.dytech.edge.common.valuebean.SearchRequest;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.services.entity.ItemDefinitionService;

public class LegacySearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	private SearchRequest request;

	public LegacySearch(SearchRequest request, ItemDefinitionService itemDefinitionService)
	{
		this.request = request;

		Collection<String> itemdefs = request.getItemdefs();
		if( itemdefs != null )
		{
			// Previous code was calling getMatchingUseableUuids, which is the
			// same as getByUuids. I think it should probably be
			// getMatchingSearchableUuids though...
			List<ItemDefinition> matchingSearchable = itemDefinitionService.getByUuids(itemdefs);
			if( matchingSearchable.isEmpty() )
			{
				try
				{
					Collection<Long> possibleIds = new ArrayList<Long>();
					for( String possibleId : request.getItemdefs() )
					{
						possibleIds.add(Long.parseLong(possibleId));
					}
					matchingSearchable = itemDefinitionService.getByIds(possibleIds);
				}
				catch( NumberFormatException ex )
				{
					// Ignore this - assume not a list of IDs
				}
			}
			setCollectionUuids(itemDefinitionService.convertToUuids(matchingSearchable));
		}

		FreeTextBooleanQuery ftquery = WhereParser.parse(request.getWhere());
		setFreeTextQuery(ftquery);

		SortType sortType1 = SortType.RANK;
		int ot = request.getOrderType();
		if( ot == SearchRequest.SORT_DATEMODIFIED )
		{
			sortType1 = SortType.DATEMODIFIED;
		}
		else if( ot == SearchRequest.SORT_DATECREATED )
		{
			sortType1 = SortType.DATECREATED;
		}
		else if( ot == SearchRequest.SORT_NAME )
		{
			sortType1 = SortType.NAME;
		}
		else if( ot == SearchRequest.SORT_RATING )
		{
			sortType1 = SortType.RATING;
		}
		else if( ot == SearchRequest.SORT_FORCOUNT )
		{
			sortType1 = SortType.FORCOUNT;
		}
		setSortFields(sortType1.getSortField(request.isSortReverse()));

		setQuery(request.getQuery());
		if( request.isOnlyLive() )
		{
			setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}
	}

	@Override
	public Date[] getDateRange()
	{
		return request.getDateRange();
	}

	@Override
	public String getOwner()
	{
		return request.getOwner();
	}
}
