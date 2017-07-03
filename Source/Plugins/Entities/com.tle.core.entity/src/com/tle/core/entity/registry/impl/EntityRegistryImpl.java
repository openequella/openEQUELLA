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
