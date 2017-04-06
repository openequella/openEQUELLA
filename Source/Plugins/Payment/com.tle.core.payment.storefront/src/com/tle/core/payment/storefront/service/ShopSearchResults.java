package com.tle.core.payment.storefront.service;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;

/**
 * @author Aaron
 */
public class ShopSearchResults extends SimpleSearchResults<StoreCatalogueItemBean>
{
	private static final long serialVersionUID = 1L;
	private final int filteredOut;

	public ShopSearchResults(List<StoreCatalogueItemBean> results, int count, int offset, int available, int filteredOut)
	{
		super(results, count, offset, available);
		this.filteredOut = filteredOut;
	}

	public int getFilteredOut()
	{
		return filteredOut;
	}
}
