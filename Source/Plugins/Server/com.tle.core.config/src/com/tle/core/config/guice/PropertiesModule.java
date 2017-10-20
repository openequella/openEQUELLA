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

package com.tle.core.config.guice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tle.common.Check;

@SuppressWarnings("nls")
public abstract class PropertiesModule extends AbstractModule
{
	protected static final LoadingCache<String, Properties> propertiesCache = CacheBuilder.newBuilder().build(
		new CacheLoader<String, Properties>()
		{
			@Override
			public Properties load(String filename)
			{
				try( InputStream propStream = PropertiesModule.class.getResourceAsStream(filename) )
				{
					Properties newProperties = new Properties();
					if( propStream != null )
					{
						newProperties.load(propStream);
					}
					return newProperties;
				}
				catch( IOException e )
				{
					throw Throwables.propagate(e);
				}
			}
		});

	private final Properties properties;

	public PropertiesModule()
	{
		properties = propertiesCache.getUnchecked(getFilename());
	}

	protected abstract String getFilename();

	private String getPropString(String property)
	{
		String envKey = "EQ_"+property.toUpperCase().replace('.', '_');
		if (System.getenv().containsKey(envKey)) {
			return System.getenv(envKey);
		}
		return (String) properties.get(property);
	}

	/**
	 * External callers may be testing for null, so allow null to propagate.
	 * 
	 * @param property
	 * @return
	 */
	protected String getProperty(String property)
	{
		String rawString = getPropString(property);
		return rawString == null ? null : rawString.trim();
	}

	protected void bindFile(String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			bind(File.class).annotatedWith(Names.named(property)).toInstance(new File(value));
		}
	}


	protected void bindProp(String property)
	{
		bindProp(property, null);
	}

	protected void bindProp(String property, String valueForMissingProperty)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( Check.isEmpty(value) )
		{
			value = valueForMissingProperty;
		}
		if( value != null )
		{
			bind(String.class).annotatedWith(Names.named(property)).toInstance(value);
		}
	}

	protected void bindInt(String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			bind(Integer.class).annotatedWith(Names.named(property)).toInstance(Integer.parseInt(value));
		}
	}

	protected void bindInt(String property, int valueForMissingProperty)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		bind(Integer.class).annotatedWith(Names.named(property)).toInstance(
			!Check.isEmpty(value) ? Integer.parseInt(value) : valueForMissingProperty);
	}

	protected void bindLong(String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			bind(Long.class).annotatedWith(Names.named(property)).toInstance(Long.parseLong(value));
		}
	}

	protected void bindBoolean(String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			bind(Boolean.class).annotatedWith(Names.named(property)).toInstance(Boolean.parseBoolean(value));
		}
	}

	protected void bindURL(String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			try
			{
				bind(URL.class).annotatedWith(Names.named(property)).toInstance(new URL(value));
			}
			catch( MalformedURLException e )
			{
				throw new ProvisionException("Invalid url in property: " + property, e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> void bindClass(TypeLiteral<T> key, String property)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			try
			{
				Class<?> clazz = getClass().getClassLoader().loadClass(value);
				bind(key).annotatedWith(Names.named(property)).toInstance((T) clazz);
			}
			catch( ClassNotFoundException e )
			{
				throw new ProvisionException("Class not found in property: " + property);
			}
		}
	}

	protected <T> void bindNewInstance(String property, Class<T> type)
	{
		String value = Strings.nullToEmpty(getPropString(property)).trim();
		if( !Check.isEmpty(value) )
		{
			try
			{
				bind(type).annotatedWith(Names.named(property)).toInstance(
					type.cast(getClass().getClassLoader().loadClass(value).newInstance()));
			}
			// In the interests of diagnostics, we'll allow an explicit catch of
			// generic exception
			catch( Exception e ) // NOSONAR
			{
				throw new ProvisionException("Class not found in property: " + property);
			}
		}
	}

	public static LoadingCache<String, Properties> getPropertiesCache()
	{
		return propertiesCache;
	}
}
