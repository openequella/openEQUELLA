/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.system.CacheSettings;
import com.tle.beans.system.CacheSettings.Node;
import com.tle.beans.system.CacheSettings.Query;
import com.tle.common.property.ConfigurationProperties;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.migration.PostReadMigrator;
import com.tle.core.services.config.ConfigurationService;

@Bind
@Singleton
public class ConfigurationConverter extends AbstractConverter<Map<String, String>>
{
	public static final String PROPERTIES_FILE = "properties/properties.xml"; //$NON-NLS-1$

	@Inject
	private ConfigurationService configurationService;

	private final List<ConfigurationStub<?>> stubs;

	public ConfigurationConverter()
	{
		stubs = new ArrayList<ConfigurationStub<?>>();
		stubs.add(new CacheConfiguration());
	}

	protected abstract class ConfigurationStub<T extends ConfigurationProperties>
	{
		public abstract T constuct();

		public void run(Map<Long, Long> old2new)
		{
			T t = constuct();
			t = configurationService.getProperties(t);
			clone(t, old2new);
			configurationService.setProperties(t);
		}

		public abstract void clone(T t, Map<Long, Long> old2new);
	}

	protected class CacheConfiguration extends ConfigurationStub<CacheSettings>
	{
		@Override
		public CacheSettings constuct()
		{
			return new CacheSettings();
		}

		@Override
		public void clone(CacheSettings empty, Map<Long, Long> old2new)
		{
			Node groups = empty.getGroups();
			if( groups != null )
			{
				recurseCache(groups, old2new);
				empty.setGroups(groups);
			}
		}

		private void recurseCache(Node groups, Map<Long, Long> old2new)
		{
			convertCacheQueries(groups.getIncludes(), old2new);
			convertCacheQueries(groups.getExcludes(), old2new);
			for( Node n : groups.getNodes() )
			{
				recurseCache(n, old2new);
			}
		}

		private void convertCacheQueries(List<Query> queries, Map<Long, Long> old2new)
		{
			for( Query q : queries )
			{
				long id = q.getItemdef();
				if( id > 0 )
				{
					Long string = old2new.get(id);
					if( string != null )
					{
						q.setItemdef(string);
					}
				}
			}
		}

	}

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		configurationService.deleteAllInstitutionProperties();
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		Map<String, String> allProperties = configurationService.getAllProperties();
		xmlHelper.writeXmlFile(staging, PROPERTIES_FILE, allProperties);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, final ConverterParams params)
		throws IOException
	{
		if( !fileSystemService.fileExists(staging, PROPERTIES_FILE) )
		{
			return;
		}

		Map<String, String> allProperties = (Map<String, String>) xmlHelper.readXmlFile(staging, PROPERTIES_FILE);
		Collection<PostReadMigrator<Map<String, String>>> migrations = getMigrations(params);
		runMigrations(migrations, allProperties);
		configurationService.importInstitutionProperties(allProperties);
		for( ConfigurationStub<?> stub : stubs )
		{
			stub.run(params.getOld2new());
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.CONFIGURATION;
	}
}
