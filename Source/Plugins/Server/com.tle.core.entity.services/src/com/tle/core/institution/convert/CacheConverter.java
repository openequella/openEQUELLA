package com.tle.core.institution.convert;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.replicatedcache.dao.ReplicatedCacheDao;

@Bind
@Singleton
@SuppressWarnings({"nls", "deprecation"})
public class CacheConverter<T> extends AbstractConverter<T>
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
