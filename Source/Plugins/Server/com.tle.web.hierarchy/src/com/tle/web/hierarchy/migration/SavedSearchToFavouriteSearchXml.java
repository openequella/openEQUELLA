package com.tle.web.hierarchy.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.tle.beans.UserPreference;
import com.tle.common.SavedSearch;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.services.InitialiserService;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SavedSearchToFavouriteSearchXml extends XmlMigrator
{
	private static final String SAVED_SEARCHES = "saved.searches";
	private static final String USERPREFS_FILE = "userprefs/preferences.xml";
	private static final String FAVSEARCHES_FOLDER = "favourites/searches";

	@Inject
	private XmlService xmlService;
	@Inject
	private SavedSearchConverter savedSearchConverter;
	@Inject
	private InitialiserService initialiserService;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile exportFolder = new SubTemporaryFile(staging, FAVSEARCHES_FOLDER);
		xmlHelper.writeExportFormatXmlFile(exportFolder, true);

		// Build list of all preferences
		List<UserPreference> userPreferences = (List<UserPreference>) xmlHelper.readXmlFile(staging, USERPREFS_FILE);

		// Check if its a saved search preference and convert searches to XML
		for( UserPreference pref : userPreferences )
		{
			if( pref.getKey().getPreferenceID().equals(SAVED_SEARCHES) )
			{
				List<FavouriteSearch> favSearches = new ArrayList<FavouriteSearch>();
				Map<String, SavedSearch> searches = xmlService.deserialiseFromXml(getClass().getClassLoader(),
					pref.getData());

				for( SavedSearch ss : searches.values() )
				{
					favSearches.add(savedSearchConverter.convertSavedSearch(instInfo.getInstitution(), pref, ss));
				}

				int count = 0;
				for( FavouriteSearch fs : favSearches )
				{
					initialiserService.initialise(fs);
					final BucketFile bucketFolder = new BucketFile(exportFolder, fs.getId());
					xmlHelper.writeXmlFile(bucketFolder, count + ".xml", fs);
					count++;
				}
			}
		}

		PropBagEx userPrefsXml = xmlHelper.readToPropBagEx(staging, USERPREFS_FILE);
		PropBagIterator iter = userPrefsXml.iterator();
		for( PropBagEx upx : iter )
		{
			if( upx.getNode("key/preferenceID").equals(SAVED_SEARCHES) )
			{
				upx.deleteNode("");
			}
		}

		// Re-write xml
		xmlHelper.writeFromPropBagEx(staging, USERPREFS_FILE, userPrefsXml);
	}
}
