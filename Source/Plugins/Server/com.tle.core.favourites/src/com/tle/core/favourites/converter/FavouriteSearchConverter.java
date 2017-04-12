package com.tle.core.favourites.converter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.dao.FavouriteSearchDao;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
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
