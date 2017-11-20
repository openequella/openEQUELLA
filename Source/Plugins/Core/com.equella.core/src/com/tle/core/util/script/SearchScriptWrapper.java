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

package com.tle.core.util.script;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.scripting.ScriptObject;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search.SortType;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.searching.SearchResults;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.user.UserService;

@Deprecated
@Bind
@Singleton
public class SearchScriptWrapper implements ScriptObject
{
	private static final long serialVersionUID = 1L;

	@Inject
	private FreeTextService freeTextService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private UserService userService;

	public DefaultSearch createSearchObject()
	{
		return new DefaultSearch();
	}

	public SearchResults search(DefaultSearch search, int start, int offset)
	{
		return freeTextService.search(search, start, offset);
	}

	public PropBagEx getXml(Item item)
	{
		return itemService.getItemXmlPropBag(item);
	}

	public ItemDefinition getItemdefFromUuid(String uuid)
	{
		return itemDefinitionService.getByUuid(uuid);
	}

	public ItemStatus getItemStatus(String status)
	{
		return ItemStatus.valueOf(status.toUpperCase());
	}

	public SortType getSortType(String type)
	{
		return SortType.valueOf(type.toUpperCase());
	}

	public Date getDate(String date, String format) throws ParseException
	{
		return new SimpleDateFormat(format).parse(date);
	}

	public FreeTextQuery getFreeTextQuery(String query)
	{
		return WhereParser.parse(query);
	}

	public List<UserBean> searchUsers(String query)
	{
		return userService.searchUsers(query);
	}

	public List<GroupBean> searchGroups(String query)
	{
		return userService.searchGroups(query);
	}

	@Override
	public void scriptEnter()
	{
		// Nothing by default
	}

	@Override
	public void scriptExit()
	{
		// Nothing by default
	}
}
