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

package com.tle.core.lti.consumers.migration;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.tle.common.Check;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.oauth.service.impl.OAuthServiceImpl;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
public class ConvertOauthClientsMigration extends AbstractHibernateDataMigration
{

	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ConvertOauthClientsMigration.class) + ".";
	private static final String OAUTH_ONE_ID = "oog";
	private static final String PROPERTY_LTI_CREATE_USERS = "lti.createusers";
	private static final String PROPERTY_ROLE_INSTRUCTOR = "role.instructor";
	private static final String PROPERTY_ROLE_OTHER = "role.other";
	private static final String LTI_PRIVILEGE = "LTI_CONSUMER";
	private static final String OAUTH_PRIVILEGE = "OAUTH_CLIENT";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.convert");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{

		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : institutions )
		{
			List<FakeOauthClient> clients = session
				.createQuery(
					"FROM OauthClient oc LEFT JOIN FETCH oc.name.strings LEFT JOIN oc.attributes attb WHERE oc.institution.id = :inst AND attb.key = :flow AND attb.value = :oauthone")
				.setParameter("inst", inst.id).setParameter("flow", OAuthServiceImpl.KEY_OAUTH_FLOW)
				.setParameter("oauthone", OAUTH_ONE_ID).list();

			if( !Check.isEmpty(clients) )
			{
				Map<String, String> ltiProps = getLtiRoleConfig(session, inst.id);

				for( FakeOauthClient client : clients )
				{
					FakeLtiConsumer consumer = new FakeLtiConsumer();
					// base entity fields
					consumer.name = copyLanguangeBundle(client.name);
					consumer.institution = client.institution;
					consumer.dateCreated = client.dateCreated;
					consumer.dateModified = new Date();
					consumer.owner = client.owner;
					consumer.uuid = UUID.randomUUID().toString();
					consumer.disabled = client.disabled;
					consumer.systemType = false;
					// client fields
					consumer.consumerKey = client.clientId;
					consumer.consumerSecret = client.clientSecret;
					consumer.allowedExpression = Recipient.EVERYONE.getPrefix();
					// lti settings fields
					if( !Check.isEmpty(ltiProps) )
					{
						if( ltiProps.containsKey(PROPERTY_LTI_CREATE_USERS) )
						{
							consumer.unknownUser = ltiProps.get(PROPERTY_LTI_CREATE_USERS).equals("true")
								? UnknownUser.CREATE.getValue() : UnknownUser.DENY.getValue();
						}
						else
						{
							consumer.unknownUser = UnknownUser.DENY.getValue();
						}
						if( ltiProps.containsKey(PROPERTY_ROLE_INSTRUCTOR) )
						{
							consumer.instructorRoles = Collections.singleton(ltiProps.get(PROPERTY_ROLE_INSTRUCTOR));
						}
						if( ltiProps.containsKey(PROPERTY_ROLE_OTHER) )
						{
							consumer.otherRoles = Collections.singleton(ltiProps.get(PROPERTY_ROLE_OTHER));
						}
					}
					else
					{
						consumer.unknownUser = UnknownUser.DENY.getValue();
					}

					session.delete(client);
					session.save(consumer);
					result.incrementStatus();
				}
			}
		}

		convertOauthClientACLs(session);
		deleteAllLtiRoleSettings(session);
		session.flush();
		session.clear();
	}

	private void deleteAllLtiRoleSettings(Session session)
	{
		session
			.createQuery(
				"DELETE FROM ConfigurationProperty WHERE key.property LIKE :create OR key.property LIKE :instructor OR key.property LIKE :other")
			.setParameter("create", PROPERTY_LTI_CREATE_USERS).setParameter("instructor", PROPERTY_ROLE_INSTRUCTOR)
			.setParameter("other", PROPERTY_ROLE_OTHER).executeUpdate();
	}

	private void convertOauthClientACLs(Session session)
	{
		@SuppressWarnings("unchecked")
		List<FakeAccessEntry> qr = session.createQuery("FROM AccessEntry where privilege LIKE '%OAUTH_CLIENT'").list();
		for( FakeAccessEntry entry : qr )
		{
			FakeAccessEntry newEntry = new FakeAccessEntry();

			newEntry.privilege = entry.privilege.replace(OAUTH_PRIVILEGE, LTI_PRIVILEGE);
			newEntry.expression = entry.expression;
			newEntry.targetObject = entry.targetObject;
			newEntry.institution = entry.institution;
			newEntry.aclOrder = entry.aclOrder;
			newEntry.grantRevoke = entry.grantRevoke;

			newEntry.aclPriority = Math.abs(entry.aclPriority) == SecurityConstants.PRIORITY_INSTITUTION
				? entry.aclPriority : entry.aclPriority < 0 ? (entry.aclOrder - 4) : (entry.aclPriority + 4);

			String aggregateOrdering = String.format("%04d %04d %c",
				(newEntry.aclPriority + SecurityConstants.PRIORITY_MAX), newEntry.aclOrder, newEntry.grantRevoke);

			newEntry.aggregateOrdering = aggregateOrdering;

			session.save(newEntry);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getLtiRoleConfig(Session session, long institute)
	{
		List<FakeConfigurationProperty> ltiProps = session
			.createQuery(
				"FROM ConfigurationProperty WHERE key.institutionId = :inst AND (key.property LIKE :create OR key.property LIKE :instructor OR key.property LIKE :other)")
			.setParameter("inst", institute).setParameter("create", PROPERTY_LTI_CREATE_USERS)
			.setParameter("instructor", PROPERTY_ROLE_INSTRUCTOR).setParameter("other", PROPERTY_ROLE_OTHER).list();
		if( !Check.isEmpty(ltiProps) )
		{
			Map<String, String> configMap = new HashMap<String, String>();
			ltiProps.stream().forEach(c -> configMap.put(c.key.property, c.value));
			return configMap;
		}
		return null;
	}

	private FakeLanguageBundle copyLanguangeBundle(FakeLanguageBundle oldBundle)
	{
		FakeLanguageBundle newBundle = new FakeLanguageBundle();
		Map<String, FakeLanguageString> strings = new HashMap<String, FakeLanguageString>();
		for( Entry<String, FakeLanguageString> entry : oldBundle.strings.entrySet() )
		{
			FakeLanguageString oldString = entry.getValue();
			FakeLanguageString newString = new FakeLanguageString();
			newString.bundle = newBundle;
			newString.locale = oldString.locale;
			newString.priority = oldString.priority;
			newString.text = oldString.text;
			strings.put(entry.getKey(), newString);
		}

		newBundle.strings = strings;
		return newBundle;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session
			.createQuery(
				"SELECT COUNT(*) FROM OauthClient oc LEFT JOIN oc.attributes attb WHERE attb.key = :flow AND attb.value = :oauthone")
			.setParameter("flow", OAuthServiceImpl.KEY_OAUTH_FLOW).setParameter("oauthone", OAUTH_ONE_ID));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeInstitution.class, FakeOauthClient.class, FakeLtiConsumer.class,
				FakeBaseEntity.class, FakeBaseEntity.Attribute.class, FakeLanguageBundle.class,
				FakeLanguageString.class, FakeConfigurationProperty.class, FakeConfigurationProperty.PropertyKey.class,
				FakeAccessEntry.class, FakeAccessExpression.class};
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}

	@Entity(name = "OauthClient")
	@AccessType("field")
	public static class FakeOauthClient extends FakeBaseEntity
	{
		@Index(name = "oauthClientIdIndex")
		@Column(nullable = false, length = 100)
		String clientId;
		@Column(nullable = false, length = 100)
		String clientSecret;
	}

	@Entity(name = "LtiConsumer")
	@AccessType("field")
	public static class FakeLtiConsumer extends FakeBaseEntity
	{
		@Index(name = "consumerKey")
		@Column(length = 255, nullable = false)
		String consumerKey;
		@Column(length = 255, nullable = false)
		String consumerSecret;
		@Column(length = 255)
		String allowedExpression;
		@ElementCollection(fetch = FetchType.LAZY)
		@CollectionTable(name = "LtiConsumer_instructorRoles", joinColumns = @JoinColumn(name = "LtiConsumer_id"))
		Set<String> instructorRoles;
		@ElementCollection(fetch = FetchType.LAZY)
		@CollectionTable(name = "LtiConsumer_otherRoles", joinColumns = @JoinColumn(name = "LtiConsumer_id"))
		Set<String> otherRoles;
		@Column
		int unknownUser;

	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 40, nullable = false)
		@Index(name = "uuidIndex")
		String uuid;
		@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
		FakeLanguageBundle name;
		@ManyToOne
		FakeInstitution institution;
		@Column(nullable = false)
		Date dateModified;
		@Column(nullable = false)
		Date dateCreated;
		@Column(nullable = false)
		@Index(name = "disabledIndex")
		boolean disabled;
		@Index(name = "baseEntitySystemTypeIndex")
		boolean systemType;
		@Type(type = "blankable")
		String owner;

		@JoinColumn
		@ElementCollection(fetch = FetchType.LAZY)
		@Fetch(value = FetchMode.SUBSELECT)
		@CollectionTable(name = "BaseEntity_attributes", joinColumns = @JoinColumn(name = "BaseEntity_id"))
		List<Attribute> attributes;

		@Embeddable
		@AccessType("field")
		public static class Attribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			String key;
			@Column(name = "value", length = 1024)
			String value;
		}
	}

	@Entity(name = "LanguageBundle")
	@AccessType("field")
	public static class FakeLanguageBundle
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
		@Fetch(value = FetchMode.SELECT)
		@MapKey(name = "locale")
		Map<String, FakeLanguageString> strings;
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 20, nullable = false)
		String locale;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		FakeLanguageBundle bundle;
		@Column
		int priority;
		@Lob
		private String text;
	}

	@Entity(name = "ConfigurationProperty")
	@AccessType("field")
	public static class FakeConfigurationProperty
	{
		@EmbeddedId
		PropertyKey key;
		@Lob
		String value;

		@Embeddable
		@AccessType("field")
		public static class PropertyKey implements Serializable
		{
			@Column
			String property;
			@Column
			long institutionId;

			@Override
			public boolean equals(Object obj)
			{
				if( this == obj )
				{
					return true;
				}

				if( !(obj instanceof PropertyKey) )
				{
					return false;
				}

				PropertyKey pkey = (PropertyKey) obj;
				return pkey.institutionId == institutionId && property.equals(pkey.property);
			}

			@Override
			public int hashCode()
			{
				return Long.valueOf(institutionId).hashCode() + property.hashCode();
			}
		}
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "accessEntryExpression")
		FakeAccessExpression expression;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		@Index(name = "accessEntryInstitution")
		FakeInstitution institution;

		@Column(length = 80)
		@Index(name = "targetObjectIndex")
		String targetObject;

		@Column(length = 30)
		@Index(name = "privilegeIndex")
		String privilege;

		@Column(length = 12, nullable = false)
		@Index(name = "aggregateOrderingIndex")
		String aggregateOrdering;

		char grantRevoke;
		int aclOrder;
		int aclPriority;
	}

	@AccessType("field")
	@Entity(name = "AccessExpression")
	public static class FakeAccessExpression
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		boolean dynamic;
		@Column(length = 1024)
		String expression;
	}

}
