package com.tle.core.institution.migration;

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
import com.tle.core.institution.convert.ItemConverter;
import com.tle.core.institution.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.ItemHelper;

@Bind
@Singleton
public class SearchDetailsUpdate implements PostReadMigrator<ItemConverter.ItemConverterInfo>
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
