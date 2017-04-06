package com.tle.core.legacy.migration.v50;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;

@Bind
@Singleton
public class RemoveExistingThumbnailsXml extends AbstractItemXmlMigrator
{

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean changed = false;
		for( PropBagEx fileAttXml : xml.iterateAll("attachments/com.tle.beans.item.attachments.FileAttachment") ) //$NON-NLS-1$
		{
			changed |= fileAttXml.deleteNode("thumbnail"); //$NON-NLS-1$
		}
		return changed;
	}
}
