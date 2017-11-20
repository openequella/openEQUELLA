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
