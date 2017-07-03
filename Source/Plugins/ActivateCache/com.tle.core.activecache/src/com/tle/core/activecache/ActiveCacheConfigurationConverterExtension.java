package com.tle.core.activecache;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.activecache.settings.CacheSettings;
import com.tle.common.activecache.settings.CacheSettings.Node;
import com.tle.common.activecache.settings.CacheSettings.Query;
import com.tle.core.guice.Bind;
import com.tle.core.settings.convert.extension.ConfigurationConverterExtension;

/**
 * @author Aaron
 *
 */
@Bind
@Singleton
public class ActiveCacheConfigurationConverterExtension extends ConfigurationConverterExtension<CacheSettings>
{
	@Override
	public CacheSettings construct()
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
