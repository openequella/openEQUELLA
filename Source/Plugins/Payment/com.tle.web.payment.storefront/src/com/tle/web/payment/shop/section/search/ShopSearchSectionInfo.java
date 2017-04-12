package com.tle.web.payment.shop.section.search;

import com.tle.common.payment.storefront.entity.Store;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author Aaron
 */
public class ShopSearchSectionInfo
{
	private final Store store;
	private final String catUuid;

	public ShopSearchSectionInfo(Store store, String catUuid)
	{
		this.store = store;
		this.catUuid = catUuid;
	}

	public Store getStore()
	{
		return store;
	}

	public String getCatUuid()
	{
		return catUuid;
	}

	public static ShopSearchSectionInfo getSearchInfo(SectionInfo info)
	{
		ShopSearchSectionInfo iinfo = info.getAttribute(ShopSearchSectionInfo.class);
		if( iinfo == null )
		{
			ShopSearchSectionInfoFactory factory = info.lookupSection(ShopSearchSectionInfoFactory.class);
			iinfo = factory.getShopSearchSectionInfo(info);
			info.setAttribute(ShopSearchSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	@TreeIndexed
	public interface ShopSearchSectionInfoFactory extends SectionId
	{
		ShopSearchSectionInfo getShopSearchSectionInfo(SectionInfo info);
	}
}
