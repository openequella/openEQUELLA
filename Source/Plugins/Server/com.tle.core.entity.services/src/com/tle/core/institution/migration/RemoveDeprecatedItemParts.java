package com.tle.core.institution.migration;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveDeprecatedItemParts extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean modified = false;
		PropBagThoroughIterator iterator = xml.iterateAll("attachments/*");
		while( iterator.hasNext() )
		{
			PropBagEx attach = iterator.next();
			if( attach.nodeExists("url2") )
			{
				attach.setNode("url", attach.getNode("url2"));
				attach.deleteNode("url2");
				modified = true;
			}
		}
		return modified;
	}
}
