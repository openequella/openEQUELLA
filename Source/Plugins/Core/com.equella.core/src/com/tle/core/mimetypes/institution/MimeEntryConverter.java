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

package com.tle.core.mimetypes.institution;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractMigratableConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.mimetypes.dao.MimeEntryDao;
import com.tle.core.hibernate.equella.service.InitialiserService;

@Bind
@Singleton
public class MimeEntryConverter extends AbstractMigratableConverter<Object>
{
	public static final String MIMETYPES_ID = "MIMEENTRIES";
	private static final String MIMETYPES_FOLDER = "mimetypes";

	@Inject
	private MimeEntryDao mimeDao;
	@Inject
	private InitialiserService initialiserService;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		List<MimeEntry> entries = mimeDao.enumerateAll();
		for( MimeEntry mimeEntry : entries )
		{
			mimeDao.delete(mimeEntry);
		}
		mimeDao.flush();
		mimeDao.clear();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		List<Long> allMimeIds = mimeDao.enumerateAllIds();
		SubTemporaryFile mimeFolder = getMimeFolder(staging);
		for( Long mimeId : allMimeIds )
		{
			MimeEntry entry = mimeDao.findById(mimeId);
			entry = initialiserService.initialise(entry);
			String filename = getFilenameForEntry(entry);
			xmlHelper.writeXmlFile(mimeFolder, filename, entry);
			mimeDao.clear();
		}
	}

	public static String getFilenameForEntry(MimeEntry entry)
	{
		return getFilenameForType(entry.getType());
	}

	public static String getFilenameForType(String type)
	{
		return type.replace('/', '_') + ".xml";
	}

	public static SubTemporaryFile getMimeFolder(TemporaryFileHandle staging)
	{
		return new SubTemporaryFile(staging, MIMETYPES_FOLDER);
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// nothing
	}

	@Override
	public void importIt(TemporaryFileHandle staging, final Institution institution, ConverterParams params, String cid)
		throws IOException
	{
		final SubTemporaryFile mimeFolder = getMimeFolder(staging);
		final List<String> entries = fileSystemService.grep(mimeFolder, "", "*.xml");
		for( final String entry : entries )
		{
			doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					MimeEntry mimeEntry = xmlHelper.readXmlFile(mimeFolder, entry);

					Map<String, String> attrs = mimeEntry.getAttributes();
					Iterator<String> iter = attrs.values().iterator();

					while( iter.hasNext() )
					{
						if( Check.isEmpty(iter.next()) )
						{
							iter.remove();
						}
					}
					mimeEntry.setInstitution(institution);
					mimeEntry.setId(0);

					mimeDao.save(mimeEntry);
					mimeDao.flush();
					mimeDao.clear();
				}
			});
		}
	}

	@Override
	public String getStringId()
	{
		return MIMETYPES_ID;
	}
}
