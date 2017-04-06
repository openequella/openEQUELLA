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
public class UpdateHistoryEventMigrator extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean changed = false;

		for( PropBagEx history : xml.iterateAll("history/com.tle.beans.item.HistoryEvent") )
		{
			history.deleteNode("item");
			PropBagEx d2 = history.getSubtree("date2");
			if( d2 != null )
			{
				d2.setNodeName("date");
			}
			changed = true;
		}

		return changed;
	}
}
