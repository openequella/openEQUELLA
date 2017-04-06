package com.tle.web.payment.shop.event;

import java.util.Date;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class ShopSearchEvent extends AbstractSearchEvent<ShopSearchEvent>
{
	private String sort;
	private boolean reverse;
	private int amount;
	private int start;
	private String priceFilter;
	private Date[] daterange;
	private int filteredOut;

	public ShopSearchEvent(SectionId sectionId)
	{
		super(sectionId);
	}

	public String getSort()
	{
		return sort;
	}

	public void setSort(String sort)
	{
		this.sort = sort;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public Date[] getDaterange()
	{
		return daterange;
	}

	public void setDaterange(Date[] dates)
	{
		this.daterange = dates;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<ShopSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public String getPriceFilter()
	{
		return priceFilter;
	}

	public void setPriceFilter(String priceFilter)
	{
		this.priceFilter = priceFilter;
	}

	public int getFilteredOut()
	{
		return filteredOut;
	}

	public void setFilteredOut(int filteredOut)
	{
		this.filteredOut = filteredOut;
	}
}