package com.tle.core.institution.migration;

import com.dytech.edge.common.Constants;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.institution.convert.ConverterParams;

public abstract class AbstractItemXmlMigrator implements ItemXmlMigrator
{
	@Override
	public void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file)
		throws Exception
	{
		// nothing
	}

	@Override
	public void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception
	{
		// nothing;
	}

	@SuppressWarnings("nls")
	public SubTemporaryFile getDataFolder(SubTemporaryFile itemsHandle, String entry)
	{
		return new SubTemporaryFile(itemsHandle, entry.replace(".xml", Constants.BLANK));
	}

	@SuppressWarnings("nls")
	public SubTemporaryFile getMetadataXml(SubTemporaryFile itemsHandle, String entry)
	{
		return new SubTemporaryFile(getDataFolder(itemsHandle, entry), "_ITEM/item.xml");
	}
}
