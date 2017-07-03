/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.item.serializer;

import java.util.Collection;
import java.util.List;

import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemExportBean;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;

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
	ItemSerializerXml createXmlSerializer(Collection<Long> itemIds, Collection<String> categories,
		String... privileges);

	/**
	 * @param itemIds
	 * @param categories
	 * @param privileges Any privileges for which you want to check with
	 *            {@link ItemSerializerItemBean#hasPrivilege(long, String)} must
	 *            be passed in here.
	 * @return
	 */
	ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		boolean export, String... privileges);

	ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		boolean ignorePriv, boolean export);

	/**
	 * @param where
	 * @param categories
	 * @param privileges Any privileges for which you want to check with
	 *            {@link ItemSerializerItemBean#hasPrivilege(long, String)} must
	 *            be passed in here.
	 * @return
	 */
	ItemSerializerItemBean createItemBeanSerializer(ItemSerializerWhere where, Collection<String> categories,
		boolean export, String... privileges);

	/**
	 * The presence of the optional 'export' flag in query parameters draws
	 * forth tailored detail.
	 * 
	 * @param equellaBean the bean identifying source Item
	 * @return ItemExportBean
	 */
	ItemExportBean getExportDetails(EquellaItemBean equellaBean);

	ItemLockBean getItemLock(EquellaItemBean equellaBean);

	List<HistoryEventBean> getHistory(String uuid, int version);
}
