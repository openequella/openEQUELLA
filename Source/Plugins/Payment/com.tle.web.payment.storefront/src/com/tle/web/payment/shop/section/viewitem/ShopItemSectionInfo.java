package com.tle.web.payment.shop.section.viewitem;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author Aaron
 */
public class ShopItemSectionInfo
{
	private final Store store;
	private final String catUuid;
	private final StoreCatalogueItemBean item;

	public ShopItemSectionInfo(Store store, String catUuid, StoreCatalogueItemBean item)
	{
		this.store = store;
		this.catUuid = catUuid;
		this.item = item;
	}

	public Store getStore()
	{
		return store;
	}

	public String getCatUuid()
	{
		return catUuid;
	}

	public StoreCatalogueItemBean getItem()
	{
		return item;
	}

	public static ShopItemSectionInfo getItemInfo(SectionInfo info)
	{
		ShopItemSectionInfo iinfo = info.getAttribute(ShopItemSectionInfo.class);
		if( iinfo == null )
		{
			ShopItemSectionInfoFactory factory = info.lookupSection(ShopItemSectionInfoFactory.class);
			iinfo = factory.createShopItemSectionInfo(info);
			info.setAttribute(ShopItemSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	@TreeIndexed
	public interface ShopItemSectionInfoFactory extends SectionId
	{
		ShopItemSectionInfo createShopItemSectionInfo(SectionInfo info);
	}
}
