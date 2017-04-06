package com.tle.core.payment.storefront.service.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.dao.PurchasedContentDao;
import com.tle.core.payment.storefront.service.PurchasedContentService;

@Bind(PurchasedContentService.class)
@Singleton
public class PurchasedContentServiceImpl implements PurchasedContentService
{
	@Inject
	private PurchasedContentDao purchasedContentDao;

	@Override
	public List<PurchasedContent> enumerateForUser(String userId)
	{
		return purchasedContentDao.enumerateForUser(userId);
	}

	@Override
	public PurchasedContent getForSourceItem(Store store, ItemId itemId)
	{
		return purchasedContentDao.getForSourceItem(store, itemId);
	}

	@Override
	public PurchasedContent getForLocalItem(ItemId itemId)
	{
		return purchasedContentDao.getForLocalItem(itemId);
	}

	@Override
	public PurchasedContent getForSourceUuid(Store store, String sourceUuid)
	{
		return purchasedContentDao.getForSourceUuid(store, sourceUuid);
	}
}
