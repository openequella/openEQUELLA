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

package com.tle.core.usermanagement.standard.convert;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.UserPreference;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.dao.UserPreferenceDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.PostReadMigrator;

@Bind
@Singleton
public class UserPreferenceConverter extends AbstractConverter<UserPreferenceConverter.UserPreferenceConverterInfo>
{
	private static final String USERPREFS_FILE = "userprefs/preferences.xml";

	@Inject
	private UserPreferenceDao userPreferenceDao;

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		List<UserPreference> allPrefs = userPreferenceDao.enumerateAll();
		for( UserPreference pref : allPrefs )
		{
			initialiserService.initialise(pref);
		}
		userPreferenceDao.clear();
		xmlHelper.writeXmlFile(staging, USERPREFS_FILE, allPrefs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		if( !fileSystemService.fileExists(staging, USERPREFS_FILE) )
		{
			return;
		}

		final List<UserPreference> allProperties = (List<UserPreference>) xmlHelper.readXmlFile(staging,
			USERPREFS_FILE);
		for( UserPreference preference : allProperties )
		{
			preference.getKey().setInstitution(institution);
		}

		// post read migrators
		final Collection<PostReadMigrator<UserPreferenceConverterInfo>> migrations = getMigrations(params);
		runMigrations(migrations, new UserPreferenceConverterInfo(allProperties, params));

		for( UserPreference preference : allProperties )
		{
			userPreferenceDao.save(preference);
			userPreferenceDao.flush();
			userPreferenceDao.clear();
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		userPreferenceDao.deleteAll();
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.PREFERENCES;
	}

	public static class UserPreferenceConverterInfo
	{
		private final List<UserPreference> prefs;
		private final ConverterParams params;

		public UserPreferenceConverterInfo(List<UserPreference> prefs, ConverterParams params)
		{
			this.prefs = prefs;
			this.params = params;
		}

		public ConverterParams getParams()
		{
			return params;
		}

		public List<UserPreference> getPrefs()
		{
			return prefs;
		}
	}
}
