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

package com.tle.core.item.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.DateHelper;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;

@NonNullByDefault
public abstract class AbstractHelper
{
	public abstract void load(PropBagEx xml, Item item);

	/**
	 * @param xml
	 * @param item
	 * @param handled The metdata paths that have been handled. The nodes found
	 *            in this list will be deleted once all helpers have had a turn
	 */
	public abstract void save(PropBagEx xml, Item item, Set<String> handled);

	public void initialise(Item item)
	{
		// TO BE OVERRIDDEN
	}

	public void load(PropBagEx item, ItemPack<Item> pack)
	{
		load(item, pack.getItem());
	}

	/**
	 * @param xml
	 * @param pack
	 * @param handled The metdata paths that have been handled. The nodes found
	 *            in this list will be deleted once all helpers have had a turn
	 */
	public void save(PropBagEx xml, ItemPack<Item> pack, Set<String> handled)
	{
		save(xml, pack.getItem(), handled);
	}

	protected void setNode(PropBagEx xml, String path, @Nullable Object o)
	{
		if( o != null )
		{
			xml.setNode(path, o.toString());
		}
		else
		{
			xml.deleteNode(path);
		}
	}

	// protected String getNode(PropBagEx xml, String path)
	// {
	// return getNode(xml, path, BLANK);
	// }
	//
	// protected String getNode(PropBagEx xml, String path, String defaultValue)
	// {
	// String value = xml.getNode(path, defaultValue);
	// xml.deleteNode(path);
	// return value;
	// }
	//
	// protected PropBagEx getSubtree(PropBagEx itemxml, String node)
	// {
	// PropBagEx value = itemxml.getSubtree(node);
	// itemxml.deleteNode(node);
	// return value;
	// }

	protected <T extends Collection<String>> T iterate(PropBagEx xml, String node, T collection)
	{
		for( String s : xml.iterateAllValues(node) )
		{
			collection.add(s);
		}
		return collection;
	}

	protected List<String> iterate(PropBagEx xml, String node)
	{
		return iterate(xml, node, new ArrayList<String>());
	}

	protected TimeZone getTimeZone()
	{
		return CurrentTimeZone.get();
	}

	protected String formatDate(@Nullable Date date)
	{
		if( date == null )
		{
			return Constants.BLANK;
		}
		return formatDate(new LocalDate(date, getTimeZone()));
	}

	@Nullable
	protected String formatDate(LocalDate date)
	{
		return date.formatOrNull(Dates.ISO_WITH_TIMEZONE);
	}

	@Nullable
	protected LocalDate parseDate(String date)
	{
		return DateHelper.parseOrNull(date, Dates.ISO_WITH_TIMEZONE, getTimeZone());
	}
}