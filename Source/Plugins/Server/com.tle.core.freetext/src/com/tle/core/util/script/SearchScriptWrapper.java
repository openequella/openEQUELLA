/*
 * Created on Sep 30, 2005
 */
package com.tle.core.util.script;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.scripting.ScriptObject;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.item.FreeTextService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.user.UserService;

@Deprecated
@Bind
@Singleton
public class SearchScriptWrapper implements ScriptObject
{
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
