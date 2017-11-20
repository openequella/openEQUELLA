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

package com.tle.core.system.migration;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.beans.ConfigurationProperty;
import com.tle.beans.DatabaseSchema;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.InstallSettings;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.beans.SystemConfig;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class InitialMigration extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(InitialMigration.class) + ".";

	@Inject
	@Named("hibernate.connection.url")
	private String systemUrl;
	@Inject
	@Named("hibernate.connection.username")
	private String systemUsername;
	@Inject
	@Named("hibernate.connection.password")
	private String systemPassword;
	@Inject
	@Named("reporting.connection.url")
	private String reportingUrl;
	@Inject
	@Named("reporting.connection.username")
	private String reportingUsername;
	@Inject
	@Named("reporting.connection.password")
	private String reportingPassword;

	@Inject
	private MigrationService migrationService;

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		Session session = helper.getFactory().openSession();
		TablesOnlyFilter filter = new TablesOnlyFilter(SystemConfig.TABLE_NAME, DatabaseSchema.TABLE_NAME);
		if( !helper.tableExists(session, ConfigurationProperty.TABLE_NAME) )
		{
			filter.setIncludeGenerators(true);
		}
		session.close();
		return helper.getCreationSql(filter);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{SystemConfig.class, ConfigurationProperty.class, DatabaseSchema.class,
				ConfigurationProperty.PropertyKey.class,};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 2;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		if( helper.tableExists(session, ConfigurationProperty.TABLE_NAME) )
		{
			// Copy over system properties that we require
			copyProperty(session, "license");
			copyProperty(session, "admin.password");
			copyProperty(session, "admin.emails");
			copyProperty(session, "smtpserver");
            copyProperty(session, "noreplysender");
			// Delete system config from old table
			session.createQuery("DELETE FROM ConfigurationProperty WHERE key.institutionId = 0").executeUpdate();
		}
		else
		{
			InstallSettings installSettings = migrationService.getInstallSettings();
			createConfig(session, "admin.password", installSettings.getHashedPassword());
			createConfig(session, "admin.emails", installSettings.getEmailsText());
			createConfig(session, "smtpserver", installSettings.getSmtpServer());
			createConfig(session, "smtpuser", installSettings.getSmtpUser());
			createConfig(session, "smtppassword", installSettings.getSmtpPassword());
            createConfig(session, "noreplysender", installSettings.getNoReplySender());

		}
		createConfig(session, "unique.id", "1");
		result.incrementStatus();

		// Create a database schema record based on hibernate.properties
		DatabaseSchema systemDB = new DatabaseSchema();
		systemDB.setDescription("Default schema");
		systemDB.setUseSystem(true);
		systemDB.setUrl(null);
		systemDB.setUsername(null);
		systemDB.setPassword(null);
		systemDB.setOnline(true);
		systemDB.setReportingUrl(nullForSame(systemUrl, reportingUrl));
		systemDB.setReportingUsername(nullForSame(systemUsername, reportingUsername));
		systemDB.setReportingPassword(nullForSame(systemPassword, reportingPassword));
		session.save(systemDB);

		result.incrementStatus();
	}

	private String nullForSame(String system, String reporting)
	{
		if( system.equals(reporting) )
		{
			return null;
		}
		return reporting;
	}

	private void copyProperty(Session session, String property)
	{
		String v = (String) session.createQuery("SELECT value FROM ConfigurationProperty WHERE property = :property")
			.setParameter("property", property).uniqueResult();

		createConfig(session, property, v);
	}

	private void createConfig(Session session, String property, String value)
	{
		SystemConfig sc = new SystemConfig();
		sc.setKey(property);
		sc.setValue(value);
		session.save(sc);
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "system.migration.title", KEY_PREFIX + "system.migration.title");
	}
}
