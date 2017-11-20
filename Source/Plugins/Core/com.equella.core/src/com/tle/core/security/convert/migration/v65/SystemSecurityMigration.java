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

package com.tle.core.security.convert.migration.v65;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.common.Check;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class SystemSecurityMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(SystemSecurityMigration.class) + ".";

	@Inject
	private EncryptionService encryptionService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.v65.system.security.title");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// System SMTP settings
		Query query = session.createQuery("FROM SystemConfig WHERE key = 'smtppassword'");
		List<FakeSystemConfig> sysconf = query.list();
		if( !Check.isEmpty(sysconf) )
		{
			FakeSystemConfig sc = sysconf.get(0); // Should only be one
			String encpwd = encryptionService.encrypt(sc.value);
			sc.value = encpwd;
			session.save(sc);
			result.incrementStatus();
		}

		// The end
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeSystemConfig.class};
	}

	@Entity(name = "SystemConfig")
	@AccessType("field")
	@Table(name = FakeSystemConfig.TABLE_NAME)
	public static class FakeSystemConfig
	{
		static final String TABLE_NAME = "sys_system_config";

		@Id
		String key;
		@Lob
		String value;
	}
}