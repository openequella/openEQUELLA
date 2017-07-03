package com.tle.core.institution.migration.v41;

import java.util.List;

import javax.inject.Singleton;

import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
public class EnsureItemFolder extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, "items");
		List<String> entries = fileSystemService.grep(folder, "", "*/_item/item.xml");
		for( String entry : entries )
		{
			entry = entry.substring(0, entry.lastIndexOf('/'));
			fileSystemService.rename(folder, entry, entry.substring(0, entry.lastIndexOf('/')) + "/_ITEM");
		}
	}
}
