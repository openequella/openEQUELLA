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

package com.tle.web.myresource.converter;

import java.io.InputStream;

import javax.inject.Singleton;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
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
			final FileHandle handle = isBucketed(itemdefFolder)
				? new BucketFile(itemdefFolder, MyContentConstants.MY_CONTENT_UUID) : itemdefFolder;
			fileSystemService.write(handle, MyContentConstants.MY_CONTENT_UUID + ".xml", in, false);
		}

		try( InputStream in = getClass().getResourceAsStream("/entities/mycontentschema.xml") )
		{
			final SubTemporaryFile schemaFolder = new SubTemporaryFile(staging, "schema");
			final FileHandle handle = isBucketed(schemaFolder)
				? new BucketFile(schemaFolder, MyContentConstants.MY_CONTENT_SCHEMA_UUID) : schemaFolder;
			fileSystemService.write(handle, MyContentConstants.MY_CONTENT_SCHEMA_UUID + ".xml", in, false);
		}
	}
}
