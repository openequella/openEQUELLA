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

package com.tle.core.portal.migration.v50;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * @author aholland
 */
@Bind
@Singleton
public class CreatePortletEntities extends AbstractCreateMigration
{
	private PluginTracker<Object> portletTracker;
	private String[] tempTables;

	public void setTempTables(String... tables)
	{
		this.tempTables = tables;
	}

	@SuppressWarnings("nls")
	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		if( tempTables != null )
		{
			return new TablesOnlyFilter(tempTables);
		}
		Set<String> tables = new HashSet<String>();
		Collections.addAll(tables, "portlet", "portlet_preference");
		List<Extension> extensions = portletTracker.getExtensions();
		for( Extension ext : extensions )
		{
			Collection<Parameter> tableParams = ext.getParameters("table");
			for( Parameter param : tableParams )
			{
				tables.add(param.valueAsString());
			}
		}
		return new TablesOnlyFilter(tables.toArray(new String[tables.size()]));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		Set<Class<?>> domainClasses = new HashSet<Class<?>>();
		Collections.addAll(domainClasses, Portlet.class, PortletPreference.class, BaseEntity.class,
			BaseEntity.Attribute.class, LanguageBundle.class, Institution.class, LanguageString.class);
		List<Extension> extensions = portletTracker.getExtensions();
		for( Extension ext : extensions )
		{
			Collection<Parameter> params = ext.getParameters("domainClass"); //$NON-NLS-1$
			for( Parameter param : params )
			{
				domainClasses.add(portletTracker.getClassForName(ext, param.valueAsString()));
			}
		}

		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(
			"com.tle.core.portal.migration.portletentities.title" + (tempTables != null ? "-remove" : ""), "");
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		portletTracker = new PluginTracker<Object>(pluginService, "com.tle.core.portal", "portletSchema", null); //$NON-NLS-1$
	}
}
