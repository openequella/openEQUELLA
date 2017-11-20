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

package com.tle.core.settings.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.TypedValue;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.net.Proxy;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.ConfigurationProperty;
import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.events.services.EventService;
import com.tle.core.services.impl.ProxyDetails;
import com.tle.core.settings.dao.ConfigurationDao;
import com.tle.core.settings.events.ConfigurationChangedEvent;
import com.tle.core.settings.events.listeners.ConfigurationChangeListener;
import com.tle.core.settings.property.PropertyBeanFactory;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.DebugSettings;

@Singleton
@SuppressWarnings("nls")
@Bind(ConfigurationService.class)
public class ConfigurationServiceImpl implements ConfigurationService, ConfigurationChangeListener
{
	private static final Logger LOGGER = Logger.getLogger(ConfigurationServiceImpl.class);

	private static final Object CACHED_NULL = new Object();
	private InstitutionCache<Cache<Object, Object>> cache;

	@Inject
	private EventService eventService;
	@Inject
	private ConfigurationDao configurationDao;

	private final ProxyDetails proxy = new ProxyDetails();

	public ConfigurationServiceImpl()
	{
		LOGGER.info("EQUELLA Version: " + ApplicationVersion.get().getFull());

		logSystemProperty("java.version", "Java Version");
		logSystemProperty("java.class.version", "Java Class Version");
		logSystemProperty("java.home", "Java Home Directory");
		logSystemProperty("os.arch", "OS Architecture");
		logSystemProperty("os.name", "OS System Name");
		logSystemProperty("os.version", "OS Version");
		logSystemProperty("user.name", "Running User");

		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Automated Test Mode:" + DebugSettings.isAutoTestMode());
			LOGGER.debug("Debugging Mode:" + DebugSettings.isDebuggingMode());
		}
	}

	private void logSystemProperty(String key, String description)
	{
		LOGGER.info(description + ": " + System.getProperty(key));
	}

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		cache = service.newInstitutionAwareCache(new CacheLoader<Institution, Cache<Object, Object>>()
		{
			@Override
			public Cache<Object, Object> load(Institution key)
			{
				return CacheBuilder.newBuilder().concurrencyLevel(12).build();
			}
		});
	}

	@Inject(optional = true)
	public void setProxyExceptions(@Named("configurationService.proxyExceptions") String proxyExceptions)
	{
		proxy.setExceptions(proxyExceptions);
	}

	@Inject(optional = true)
	public void setProxyHost(@Named("configurationService.proxyHost") String proxyHost)
	{
		proxy.setHost(proxyHost);
	}

	@Inject(optional = true)
	public void setProxyPassword(@Named("configurationService.proxyPassword") String proxyPassword)
	{
		proxy.setPassword(proxyPassword);
	}

	@Inject(optional = true)
	public void setProxyPort(@Named("configurationService.proxyPort") int proxyPort)
	{
		proxy.setPort(proxyPort);
	}

	@Inject(optional = true)
	public void setProxyUsername(@Named("configurationService.proxyUsername") String proxyUsername)
	{
		proxy.setUsername(proxyUsername);
	}

	@PostConstruct
	public void afterPropertiesSet()
	{
		Proxy.setProxy(proxy.getHost(), proxy.getPort(), proxy.getExceptions(), proxy.getUsername(),
			proxy.getPassword());
	}

	@Override
	@Transactional
	public Map<String, String> getAllProperties()
	{
		Criterion crit = getInstitutionCriterion();
		List<ConfigurationProperty> all = configurationDao.findAllByCriteria(crit);
		return PropertyBeanFactory.fill(all);
	}

	// Appearances aside, the getPropertires is effectively synchronized so long
	// as getFromCache is
	@Override
	public <T extends ConfigurationProperties> T getProperties(final T empty)
	{
		return getFromCache(empty.getClass(), null, new CacheLoader<String, T>() // NOSONAR
		{
			@Override
			public T load(String property)
			{
				Collection<String> queries = PropertyBeanFactory.getSelect(empty);
				MultipleOr criterion = new MultipleOr();
				for( String query : queries )
				{
					Criterion crit = Restrictions.like("key.property", query + "%");
					criterion.add(crit);
				}

				List<ConfigurationProperty> all;
				if( queries.size() > 0 )
				{
					all = configurationDao.findAllByCriteria(getInstitutionCriterion(), criterion);
				}
				else
				{
					all = new ArrayList<ConfigurationProperty>();
				}
				PropertyBeanFactory.fill(all, empty);
				return empty;
			}
		});
	}

	@Override
	public String getProperty(String property)
	{
		return getFromCache(property, property, new CacheLoader<String, String>()
		{
			@Override
			public String load(String property)
			{
				PropertyKey key = new PropertyKey(CurrentInstitution.get(), property);
				ConfigurationProperty prop = configurationDao.findById(key);
				return prop == null ? null : prop.getValue();
			}
		});
	}

	@Override
	public <T> List<T> getPropertyList(final String property)
	{
		return getFromCache(property, property, new CacheLoader<String, List<T>>()
		{
			@Override
			public List<T> load(String property)
			{
				Criterion crit = Restrictions.like("key.property", property + "%");
				Criterion crit2 = getInstitutionCriterion();
				Map<String, String> vals = PropertyBeanFactory.fill(configurationDao.findAllByCriteria(crit, crit2));

				List<T> list = new ArrayList<T>();
				PropertyBeanFactory.load(list, property, vals);
				return list;
			}
		});
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void importInstitutionProperties(Map<String, String> map)
	{
		setPropertiesImpl(map);
	}

	@Override
	@Transactional
	public void setProperty(String property, String value)
	{
		setPropertyImpl(property, value);
		invalidateCache();
	}

	@Override
	@Transactional
	public synchronized void setProperties(ConfigurationProperties properties)
	{
		Collection<String> select = PropertyBeanFactory.getSelect(properties);
		if( select.size() > 0 )
		{
			configurationDao.deletePropertiesLike(select);

			HashMap<String, String> map = new HashMap<String, String>();
			PropertyBeanFactory.save(properties, map);
			setPropertiesImpl(map);
		}

		invalidateCache();
	}

	private void setPropertiesImpl(Map<String, String> map)
	{
		for( Map.Entry<String, String> entry : map.entrySet() )
		{
			setPropertyImpl(entry.getKey(), entry.getValue());
		}
	}

	private void setPropertyImpl(String property, String value)
	{
		ConfigurationProperty prop = new ConfigurationProperty();
		PropertyKey key = new PropertyKey(CurrentInstitution.get(), property);
		prop.setValue(value);
		prop.setKey(key);
		configurationDao.merge(prop);
	}

	@Override
	@Transactional
	public void deleteProperty(String property)
	{
		configurationDao.deletePropertiesLike(Collections.singleton(property));
		invalidateCache();
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void deleteAllInstitutionProperties()
	{
		invalidateCache();
		configurationDao.deleteAll();
	}

	private Criterion getInstitutionCriterion()
	{
		Institution inst = CurrentInstitution.get();
		if( inst != null )
		{
			return Restrictions.eq("key.institutionId", inst.getDatabaseId());
		}
		throw new IllegalStateException("Configuration properties can only be retrieved from within an institution");
	}

	@Override
	public boolean isAutoTestMode()
	{
		return DebugSettings.isAutoTestMode();
	}

	@Override
	public boolean isDebuggingMode()
	{
		return DebugSettings.isDebuggingMode();
	}

	@Override
	public ProxyDetails getProxyDetails()
	{
		return proxy;
	}

	// CACHING ///////////////////////////////////////////////////////////////

	@Override
	public void configurationChangedEvent(ConfigurationChangedEvent event)
	{
		cache.clear();
	}

	private void invalidateCache()
	{
		ConfigurationChangedEvent event = new ConfigurationChangedEvent();

		// Call to local handler method to invalidate the local cache, then post
		// it to everyone else.
		configurationChangedEvent(event);

		eventService.publishApplicationEvent(event);
	}

	@Transactional
	<T> T loadFromDb(String property, CacheLoader<String, T> loader)
	{
		try
		{
			return loader.load(property);
		}
		catch( Exception e )
		{
			throw new RuntimeApplicationException("Could not create config object", e); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getFromCache(Object key, String property, final CacheLoader<String, T> loader)
	{
		Cache<Object, Object> map = cache.getCache();

		Object ro = map.getIfPresent(key);
		if( ro == null )
		{
			T newObj = loadFromDb(property, loader);
			synchronized( map )
			{
				map.put(key, newObj != null ? newObj : CACHED_NULL);
				return newObj;
			}
		}
		else
		{
			return ro.equals(CACHED_NULL) ? null : (T) ro;
		}
	}

	// HIBERNATE /////////////////////////////////////////////////////////////

	private static class MultipleOr implements Criterion
	{
		private static final long serialVersionUID = 1L;

		private final List<Criterion> criterion;
		private final String op;

		protected MultipleOr()
		{
			criterion = new ArrayList<Criterion>();
			this.op = "or";
		}

		public void add(Criterion crit)
		{
			criterion.add(crit);
		}

		@Override
		public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException
		{
			List<TypedValue> values = new ArrayList<TypedValue>();
			for( Criterion crit : criterion )
			{
				values.addAll(Arrays.asList(crit.getTypedValues(criteria, criteriaQuery)));
			}
			return values.toArray(new TypedValue[values.size()]);
		}

		@Override
		public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append('(');
			int i = 0;
			for( Criterion crit : criterion )
			{
				if( i != 0 )
				{
					buffer.append(' ');
					buffer.append(op);
					buffer.append(' ');
				}
				buffer.append(crit.toSqlString(criteria, criteriaQuery));
				i++;
			}
			buffer.append(')');
			return buffer.toString();
		}

		@Override
		public String toString()
		{
			StringBuilder buffer = new StringBuilder();
			int i = 0;
			for( Criterion crit : criterion )
			{
				if( i != 0 )
				{
					buffer.append(' ');
					buffer.append(op);
					buffer.append(' ');
				}
				buffer.append(crit.toString());
				i++;
			}
			return buffer.toString();
		}
	}
}
