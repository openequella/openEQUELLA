package com.tle.core.item.serializer;

import java.util.Collection;

@SuppressWarnings("nls")
public interface ItemSerializerService
{
	String CATEGORY_BASIC = "basic";
	String CATEGORY_DETAIL = "detail";
	String CATEGORY_METADATA = "metadata";
	String CATEGORY_ATTACHMENT = "attachment";
	String CATEGORY_ALL = "all";

	/**
	 * @param itemIds
	 * @param categories
	 * @param privileges Any privileges for which you want to check with
	 *            {@link ItemSerializerItemBean#hasPrivilege(long, String)} must
	 *            be passed in here.
	 * @return
	 */
	ItemSerializerXml createXmlSerializer(Collection<Long> itemIds, Collection<String> categories, String... privileges);

	/**
	 * @param itemIds
	 * @param categories
	 * @param privileges Any privileges for which you want to check with
	 *            {@link ItemSerializerItemBean#hasPrivilege(long, String)} must
	 *            be passed in here.
	 * @return
	 */
	ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		String... privileges);

	ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		boolean ignorePriv);

	/**
	 * @param where
	 * @param categories
	 * @param privileges Any privileges for which you want to check with
	 *            {@link ItemSerializerItemBean#hasPrivilege(long, String)} must
	 *            be passed in here.
	 * @return
	 */
	ItemSerializerItemBean createItemBeanSerializer(ItemSerializerWhere where, Collection<String> categories,
		String... privileges);
}
