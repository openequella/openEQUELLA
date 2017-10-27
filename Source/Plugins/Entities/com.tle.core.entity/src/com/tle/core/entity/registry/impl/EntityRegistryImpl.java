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

package com.tle.core.entity.registry.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;

@Bind(EntityRegistry.class)
@Singleton
public class EntityRegistryImpl implements EntityRegistry
{
	private Map<Class<? extends BaseEntity>, AbstractEntityService<?, BaseEntity>> services;

	@Inject
	private PluginTracker<AbstractEntityService<?, BaseEntity>> tracker;

	@Override
	public List<AbstractEntityService<?, BaseEntity>> getAllEntityServices()
	{
		return ensureCache();
	}

	@Override
	public synchronized AbstractEntityService<?, BaseEntity> getServiceForClass(Class<? extends BaseEntity> clazz)
	{
		List<AbstractEntityService<?, BaseEntity>> beanList = ensureCache();
		if( services == null )
		{
			services = new HashMap<Class<? extends BaseEntity>, AbstractEntityService<?, BaseEntity>>();
			for( AbstractEntityService<?, BaseEntity> bean : beanList )
			{
				services.put(bean.getEntityClass(), bean);
				List<Class<? extends BaseEntity>> additional = bean.getAdditionalEntityClasses();
				if( additional != null )
				{
					for( Class<? extends BaseEntity> additionalClass : additional )
					{
						services.put(additionalClass, bean);
					}
				}
			}
		}
		return services.get(clazz);
	}

	private synchronized List<AbstractEntityService<?, BaseEntity>> ensureCache()
	{
		if( tracker.needsUpdate() )
		{
			services = null;
		}
		return tracker.getBeanList();
	}
}
