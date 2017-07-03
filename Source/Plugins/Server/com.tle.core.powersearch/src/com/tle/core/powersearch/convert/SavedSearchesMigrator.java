package com.tle.core.powersearch.convert;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.UserPreference;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.SavedSearch;
import com.tle.core.collection.dao.ItemDefinitionDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.institution.migration.v41.FixSavedSearches;
import com.tle.core.institution.migration.v41.FixSavedSearches.SavedSearchFixerIdResolver;
import com.tle.core.powersearch.PowerSearchDao;
import com.tle.core.usermanagement.standard.convert.UserPreferenceConverter.UserPreferenceConverterInfo;
import com.tle.core.xml.service.XmlService;

/**
 * To be honest, this should probably be in another plugin?  Needs access to power search dao though.
 * 
 * @author aholland
 */
@Bind
@Singleton
public class SavedSearchesMigrator implements PostReadMigrator<UserPreferenceConverterInfo>
{
	@Inject
	private XmlService xmlService;
	@Inject
	private ItemDefinitionDao collectionDao;
	@Inject
	private PowerSearchDao powerDao;

	@Override
	public void migrate(UserPreferenceConverterInfo migrationInfo) throws IOException
	{
		for( final UserPreference pref : migrationInfo.getPrefs() )
		{
			if( pref.getKey().getPreferenceID().equals("saved.searches") ) //$NON-NLS-1$
			{
				final Map<String, SavedSearch> searches = xmlService.deserialiseFromXml(getClass().getClassLoader(),
					pref.getData());

				FixSavedSearches.convertSavedSearches(searches.values(),
					new InstitutionImportSavedSearchFixerIdResolver(migrationInfo.getParams().getOld2new()));

				pref.setData(xmlService.serialiseToXml(searches));
			}
		}
	}

	protected class InstitutionImportSavedSearchFixerIdResolver implements SavedSearchFixerIdResolver
	{
		private final Map<Long, Long> entityIdMap;

		protected InstitutionImportSavedSearchFixerIdResolver(Map<Long, Long> entityIdMap)
		{
			this.entityIdMap = entityIdMap;
		}

		@Override
		public String resolveToCollectionUuid(Long entId)
		{
			// look up the new ID:
			Long newId = entityIdMap.get(entId);
			if( newId != null )
			{
				final ItemDefinition ent = collectionDao.findById(newId);
				if( ent != null )
				{
					return ent.getUuid();
				}
			}
			return null;
		}

		@Override
		public String resolveToPowerUuid(Long entId)
		{
			// look up the new ID:
			Long newId = entityIdMap.get(entId);
			if( newId != null )
			{
				final PowerSearch ent = powerDao.findById(entId);
				if( ent != null )
				{
					return ent.getUuid();
				}
			}
			return null;
		}
	}
}
