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

package com.tle.core.favourites.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.dao.FavouriteSearchDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractMigratableConverter;
import com.tle.core.institution.convert.ConverterParams;

@SuppressWarnings("nls")
@Bind
@Singleton
public class FavouriteSearchConverter extends AbstractMigratableConverter<FavouriteSearch>
{
	public static final String FAVOURITESEARCHES_ID = "FAVOURITESEARCHES";
	private static final String FAVSEARCHES_FOLDER = "favourites/searches";

	@Inject
	private FavouriteSearchDao favSearchDao;

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		SubTemporaryFile exportFolder = new SubTemporaryFile(staging, FAVSEARCHES_FOLDER);
		// write out the format details
		xmlHelper.writeExportFormatXmlFile(exportFolder, true);

		List<FavouriteSearch> favSearches = favSearchDao.enumerateAll();
		for( FavouriteSearch fs : favSearches )
		{
			initialiserService.initialise(fs);
			fs.setInstitution(null);
			final BucketFile bucketFolder = new BucketFile(exportFolder, fs.getId());
			xmlHelper.writeXmlFile(bucketFolder, fs.getId() + ".xml", fs);
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		favSearchDao.deleteAll();
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile importFolder = new SubTemporaryFile(staging, FAVSEARCHES_FOLDER);
		final List<String> entries = xmlHelper.getXmlFileList(importFolder);

		for( String entry : entries )
		{
			final FavouriteSearch favSearch = xmlHelper.readXmlFile(importFolder, entry);
			favSearch.setInstitution(institution);
			favSearch.setId(0);

			favSearchDao.save(favSearch);
			favSearchDao.flush();
			favSearchDao.clear();
		}
	}

	@Override
	public String getStringId()
	{
		return FAVOURITESEARCHES_ID;
	}
}
