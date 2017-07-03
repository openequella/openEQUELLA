package com.tle.core.institution.migration.v41;

import java.util.Iterator;
import java.util.UUID;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CommentUuidXmlMigration extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		Iterator<PropBagEx> iter = xml.iterateAll("comments/com.tle.beans.item.Comment");
		boolean modified = false;

		while( iter.hasNext() )
		{
			PropBagEx comment = iter.next();
			if( Check.isEmpty(comment.getNode("uuid")) )
			{
				comment.setNode("uuid", UUID.randomUUID().toString());
				modified = true;
			}
		}

		return modified;
	}
}
