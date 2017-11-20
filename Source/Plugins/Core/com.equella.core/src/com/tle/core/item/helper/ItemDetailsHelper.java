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

import java.util.Arrays;
import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.base.Strings;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.LocalDate;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ItemDetailsHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx item, ItemPack pack)
	{
		setNode(item, "/staging", pack.getStagingID());
		super.load(item, pack);
	}

	@Override
	public void load(PropBagEx xml, Item item)
	{
		// The following two nodes are for backwards compatibility only. We do
		// not "save" these XPaths back into the beans.
		String nameFromBean = CurrentLocale.get(item.getName(), null);
		setNode(xml, "/name", nameFromBean);
		setNode(xml, "/description", CurrentLocale.get(item.getDescription(), null));

		setNode(xml, "/@key", item.getId());
		setNode(xml, "/@id", item.getUuid());
		setNode(xml, "/@version", item.getVersion());
		setNode(xml, "newitem", item.isNewItem());

		final String thumb = item.getThumb();
		if( thumb != null )
		{
			setNode(xml, "thumbnail", thumb);
		}
		final ItemDefinition itemdef = item.getItemDefinition();
		if( itemdef != null )
		{
			setNode(xml, "/@itemdefid", itemdef.getUuid());
		}

		setNode(xml, "/owner", item.getOwner());
		setNode(xml, "/datecreated", formatDate(item.getDateCreated()));
		setNode(xml, "/datemodified", formatDate(item.getDateModified()));
		setNode(xml, "/dateforindex", formatDate(item.getDateForIndex()));
		setNode(xml, "/@itemstatus", item.getStatus());
		setNode(xml, "/@moderating", item.isModerating());
		setNode(xml, "/rating/@average", item.getRating());
	}

	@Override
	public void save(PropBagEx xml, ItemPack<Item> pack, Set<String> handled)
	{
		pack.setStagingID(xml.getNode("staging"));
		handled.add("staging");
		super.save(xml, pack, handled);
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		String key = xml.getNode("@key");
		if( !Check.isEmpty(key) )
		{
			item.setId(Long.parseLong(key));
		}

		item.setUuid(xml.getNode("@id"));
		item.setVersion(xml.getIntNode("@version", 1));

		item.setNewItem(xml.getNode("newitem").equals(Constants.XML_TRUE));
		item.setOwner(xml.getNode("owner"));

		LocalDate local = parseDate(xml.getNode("datecreated"));
		item.setDateCreated(local == null ? null : local.toDate());
		local = parseDate(xml.getNode("datemodified"));
		item.setDateModified(local == null ? null : local.toDate());
		item.setStatus(ItemHelper.statusStringToEnum(xml.getNode("@itemstatus")));
		final String thumb = xml.getNode("thumbnail");
		if( !Strings.isNullOrEmpty(thumb) )
		{
			item.setThumb(thumb);
		}

		// Just need to remove
		xml.deleteAll("itembody/thumbnail");
		xml.deleteAll("@live");
		xml.deleteAll("@moderating");
		xml.deleteAll("itemtype");
		xml.deleteAll("modifier");
		xml.deleteAll("lockedby");
		xml.deleteAll("publiclock");

		float rating = -1f;
		try
		{
			rating = Float.parseFloat(xml.getNode("rating/@average"));
		}
		catch( NumberFormatException ex )
		{
			// Just ignore
		}
		item.setRating(rating);

		String itemdefid = xml.getNode("@itemdefid");
		// This may not work
		if( item.getItemDefinition() == null || !item.getItemDefinition().getUuid().equals(itemdefid) )
		{
			ItemDefinition itemdef = new ItemDefinition();
			itemdef.setUuid(itemdefid);
			item.setItemDefinition(itemdef);
		}

		handled.addAll(Arrays.asList(new String[]{"@key", "@id", "@task", "newitem", "owner", "datecreated",
				"datemodified", "dateforindex", "@itemstatus", "itembody/thumbnail", "@live", "@moderating", "itemtype",
				"modifier", "lockedby", "publiclock", "folder", "rating/@average", "@itemdefid", "thumbnail"}));
	}
}