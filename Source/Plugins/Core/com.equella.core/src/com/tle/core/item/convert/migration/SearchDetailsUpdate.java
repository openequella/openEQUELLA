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

package com.tle.core.item.convert.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
public class SearchDetailsUpdate implements PostReadMigrator<ItemConverterInfo>
{
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		Item item = obj.getItem();
		Map<Long, SearchDetails> detailsMap = obj.getState(this);
		if( detailsMap == null )
		{
			detailsMap = new HashMap<Long, SearchDetails>();
			obj.setState(this, detailsMap);
		}
		PropBagEx itemxml;
		long itemdefId = item.getItemDefinition().getId();
		SearchDetails details = null;
		if( !detailsMap.containsKey(itemdefId) )
		{
			details = item.getItemDefinition().getSearchDetails();
			detailsMap.put(itemdefId, details);
		}
		else
		{
			details = detailsMap.get(itemdefId);
		}
		if( details != null && !Check.isEmpty(details.getDisplayNodes()) )
		{
			try( InputStream stream = fileSystemService.read(obj.getFileHandle(), "_ITEM/item.xml") )
			{
				itemxml = new PropBagEx(stream);
				Collection<DisplayNode> nodes = details.getDisplayNodes();
				item.setSearchDetails(ItemHelper.getValuesForDisplayNodes(nodes, itemxml));
			}
		}
	}
}
