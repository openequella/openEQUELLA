package com.tle.core.institution.migration.v32;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UpdateItemNavigationTree extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean changed = false;

		for( PropBagEx node : xml.iterateAll("treeNodes/com.tle.beans.item.attachments.ItemNavigationNode") )
		{
			PropBagEx attachment = node.getSubtree("attachment");
			if( attachment != null )
			{
				PropBagEx tab = node.newSubtree("tabs/com.tle.beans.item.attachments.ItemNavigationTab");
				tab.setNode("id", -1);
				tab.setNode("node/@reference", "../../..");
				tab.setNode("name", "Node converted from 3.1");
				tab.setNode("viewer", "");
				tab.setNode("attachment/@class", attachment.getNode("@class"));
				tab.setNode("attachment/@reference", "../../" + attachment.getNode("@reference"));

				node.deleteNode("attachment");

				changed = true;
			}
		}

		changed = xml.deleteNode("navigationSettings/showNextPrev") || changed;

		return changed;
	}
}
