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

package com.tle.core.replicatedcache.convert;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.replicatedcache.dao.CachedValue;
import com.tle.core.replicatedcache.dao.ReplicatedCacheDao;

@Bind
@Singleton
@SuppressWarnings({"nls", "deprecation"})
public class ReplicatedCacheConverter extends AbstractConverter<CachedValue>
{
	@Inject
	private ReplicatedCacheDao replicatedCacheDao;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		// Clean out DB cached_values for this institution
		replicatedCacheDao.invalidateAllForInstitution(institution);
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// SHITBAG 1
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		// SHITBAG 2
	}

	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	public String getStringId()
	{
		return "REPLICATEDCACHE";
	}
}
