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

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class IncreaseColumnForEncryptionMigration extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(IncreaseColumnForEncryptionMigration.class)
		+ ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.v65.increaseforencryption.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session.createQuery("UPDATE OAuthClient SET tempClientSecret = clientSecret").executeUpdate();
		session.createQuery("UPDATE LtiConsumer SET tempConsumerSecret = consumerSecret").executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 2;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		String origOAuthColumn = "client_secret";
		String origLTIColumn = "consumer_secret";

		List<String> modSQL = helper.getDropColumnSQL(FakeOAuthClient.TABLE_NAME, origOAuthColumn);
		modSQL.addAll(helper.getDropColumnSQL(FakeLtiConsumer.TABLE_NAME, origLTIColumn));

		modSQL.addAll(helper.getRenameColumnSQL(FakeOAuthClient.TABLE_NAME, "temp_client_secret", origOAuthColumn));
		modSQL.addAll(helper.getRenameColumnSQL(FakeLtiConsumer.TABLE_NAME, "temp_consumer_secret", origLTIColumn));

		return modSQL;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> addSQL = helper.getAddColumnsSQL(FakeOAuthClient.TABLE_NAME, "temp_client_secret");
		addSQL.addAll(helper.getAddColumnsSQL(FakeLtiConsumer.TABLE_NAME, "temp_consumer_secret"));
		return addSQL;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeOAuthClient.class, FakeLtiConsumer.class};
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
		static String TABLE_NAME = "oauth_client";

		@Index(name = "oauthClientIdIndex")
		@Column(nullable = false, length = 100)
		String clientId;

		@Lob
		String clientSecret;

		@Lob
		String tempClientSecret;
	}

	@Entity(name = "LtiConsumer")
	@AccessType("field")
	public static class FakeLtiConsumer extends FakeBaseEntity
	{
		static String TABLE_NAME = "lti_consumer";

		@Index(name = "consumerKey")
		@Column(length = 255, nullable = false)
		String consumerKey;

		@Lob
		String consumerSecret;

		@Lob
		String tempConsumerSecret;
	}

}
