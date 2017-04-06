package com.tle.web.myresource.converter;

import java.io.InputStream;

import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.mycontent.MyContentConstants;

@Bind
@Singleton
public class MyResourceSchemaMigrator extends XmlMigrator
{
	@SuppressWarnings("nls")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		try( InputStream in = getClass().getResourceAsStream("/entities/mycontentcollection.xml") )
		{
			final SubTemporaryFile itemdefFolder = new SubTemporaryFile(staging, "itemdefinition");
			final FileHandle handle = isBucketed(itemdefFolder) ? new BucketFile(itemdefFolder,
				MyContentConstants.MY_CONTENT_UUID) : itemdefFolder;
			fileSystemService.write(handle, MyContentConstants.MY_CONTENT_UUID + ".xml", in, false);
		}

		try( InputStream in = getClass().getResourceAsStream("/entities/mycontentschema.xml") )
		{
			final SubTemporaryFile schemaFolder = new SubTemporaryFile(staging, "schema");
			final FileHandle handle = isBucketed(schemaFolder) ? new BucketFile(schemaFolder,
				MyContentConstants.MY_CONTENT_SCHEMA_UUID) : schemaFolder;
			fileSystemService.write(handle, MyContentConstants.MY_CONTENT_SCHEMA_UUID + ".xml", in, false);
		}
	}
}
