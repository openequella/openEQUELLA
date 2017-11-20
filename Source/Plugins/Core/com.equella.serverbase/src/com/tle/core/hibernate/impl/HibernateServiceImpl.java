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

package com.tle.core.hibernate.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.springframework.orm.hibernate3.SpringSessionContext;
import org.springframework.orm.hibernate3.SpringTransactionFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.HibernateFactory;
import com.tle.core.hibernate.HibernateFactoryService;
import com.tle.core.hibernate.HibernateService;

@Bind(HibernateService.class)
@Singleton
public class HibernateServiceImpl implements HibernateService
{
	@Inject
	private HibernateFactoryService hibernateService;
	@Inject
	private DataSourceService datasourceService;
	@Inject
	private DynamicDataSource institutionAwareDataSource;

	private LoadingCache<SessionFactoryKey, SessionFactory> factories = CacheBuilder.newBuilder().build(
		new CacheLoader<SessionFactoryKey, SessionFactory>()
		{
			@Override
			public SessionFactory load(SessionFactoryKey key)
			{
				return getHibernateFactory(key.getName(), key.isSystemOnly()).getSessionFactory();
			}
		});

	protected HibernateFactory getHibernateFactory(String name, boolean system)
	{
		Class<?>[] clazzes = hibernateService.getDomainClasses(name);
		DataSourceHolder dataSource;
		if( system )
		{
			dataSource = datasourceService.getSystemDataSource();
		}
		else
		{
			dataSource = new DataSourceHolder(institutionAwareDataSource, datasourceService.getDialect());
		}
		HibernateFactory factory = hibernateService.createConfiguration(dataSource, clazzes);
		factory.setClassLoader(getClass().getClassLoader());
		factory.setProperty(Environment.TRANSACTION_STRATEGY, SpringTransactionFactory.class.getName());
		factory.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, SpringSessionContext.class.getName());
		return factory;
	}

	@Override
	public synchronized SessionFactory getTransactionAwareSessionFactory(String name, boolean system)
	{
		return factories.getUnchecked(new SessionFactoryKey(name, system));
	}

	public static class SessionFactoryKey
	{
		private final String name;
		private final boolean systemOnly;

		public SessionFactoryKey(String name, boolean systemOnly)
		{
			this.name = name;
			this.systemOnly = systemOnly;
		}

		public String getName()
		{
			return name;
		}

		public boolean isSystemOnly()
		{
			return systemOnly;
		}

		@Override
		public int hashCode()
		{
			return name.hashCode() + (systemOnly ? 1 : 0);
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof SessionFactoryKey) )
			{
				return false;
			}

			SessionFactoryKey other = (SessionFactoryKey) obj;
			return name.equals(other.name) && systemOnly == other.systemOnly;
		}
	}
}
