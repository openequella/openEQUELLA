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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class SecurityMigration extends AbstractHibernateDataMigration
{
	private static final String SMTP_PWD_KEY = "mail.password";
	private static final String LDAP_PWD_KEY = "ldap.admin.password";
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(SecurityMigration.class) + ".";

	@Inject
	private EncryptionService encryptionService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.v65.security.title");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// OAuth secrets
		Query query = session.createQuery("FROM OAuthClient");
		List<FakeOAuthClient> oaclist = query.list();

		for( FakeOAuthClient oac : oaclist )
		{
			String oldSecret = oac.clientSecret;
			oac.clientSecret = encryptionService.encrypt(oldSecret);
			session.save(oac);
			result.incrementStatus();
		}

		// LTI secrets
		query = session.createQuery("FROM LtiConsumer");
		List<FakeLtiConsumer> lclist = query.list();

		for( FakeLtiConsumer lc : lclist )
		{
			String oldSecret = lc.consumerSecret;
			lc.consumerSecret = encryptionService.encrypt(oldSecret);
			session.save(lc);
			result.incrementStatus();
		}

		// Institution SMTP and LDAP settings
		query = session.createQuery("FROM ConfigurationProperty WHERE property = :smtp OR property = :ldap")
			.setParameter("smtp", SMTP_PWD_KEY).setParameter("ldap", LDAP_PWD_KEY);
		List<FakeConfigurationProperty> configs = query.list();

		for( FakeConfigurationProperty cp : configs )
		{
			String pwdencrypted = encryptionService.encrypt(cp.value);
			cp.value = pwdencrypted;
			session.save(cp);
			result.incrementStatus();
		}

		// The end
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		int lti = count(session, "FROM LtiConsumer");
		int oauth = count(session, "FROM OAuthClient");
		int ldapsmtp = count(
			session.createQuery("SELECT COUNT(*) FROM ConfigurationProperty WHERE property = :smtp OR property = :ldap")
				.setString("smtp", SMTP_PWD_KEY).setString("ldap", LDAP_PWD_KEY));

		return lti + oauth + ldapsmtp;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeConfigurationProperty.class, FakeBaseEntity.class, FakeOAuthClient.class,
				FakeLtiConsumer.class};
	}

	@Entity(name = "ConfigurationProperty")
	@AccessType("field")
	public static class FakeConfigurationProperty
	{
		@EmbeddedId
		PropertyKey key;

		@Lob
		String value;
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}

	@Entity(name = "OAuthClient")
	@AccessType("field")
	public static class FakeOAuthClient extends FakeBaseEntity
	{
		@Index(name = "oauthClientIdIndex")
		@Column(nullable = false, length = 100)
		String clientId;

		@Lob
		String clientSecret;
	}

	@Entity(name = "LtiConsumer")
	@AccessType("field")
	public static class FakeLtiConsumer extends FakeBaseEntity
	{
		@Index(name = "consumerKey")
		@Column(length = 255, nullable = false)
		String consumerKey;

		@Lob
		String consumerSecret;
	}
}
