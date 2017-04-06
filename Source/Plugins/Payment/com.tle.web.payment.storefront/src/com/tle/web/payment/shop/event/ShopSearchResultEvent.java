package com.tle.web.payment.shop.event;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.storefront.service.ShopSearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class ShopSearchResultEvent extends AbstractSearchResultsEvent<ShopSearchResultEvent>
{
	private final Store store;
	private final String catUuid;
	private final ShopSearchResults results;
	private final int filteredOut;
	private final ShopSearchEvent event;

	public ShopSearchResultEvent(ShopSearchEvent searchEvent, Store store, String catUuid, ShopSearchResults results,
		int filteredOut)
	{
		this.store = store;
		this.catUuid = catUuid;
		this.results = results;
		this.filteredOut = filteredOut;
		this.event = searchEvent;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<ShopSearchResultEvent> listener)
		throws Exception
	{
		listener.processResults(info, this);
	}

	@Override
	public int getOffset()
	{
		return results.getOffset();
	}

	@Override
	public int getCount()
	{
		return results.getCount();
	}

	@Override
	public int getMaximumResults()
	{
		return results.getAvailable();
	}

	@Override
	public int getFilteredOut()
	{
		return filteredOut;
	}

	public Store getStore()
	{
		return store;
	}

	public String getCatUuid()
	{
		return catUuid;
	}

	public ShopSearchResults getResults()
	{
		return results;
	}

	public ShopSearchEvent getEvent()
	{
		return event;
	}
}