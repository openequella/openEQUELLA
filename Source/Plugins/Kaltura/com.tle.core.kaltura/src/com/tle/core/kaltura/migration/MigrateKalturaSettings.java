package com.tle.core.kaltura.migration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.classic.Session;

import com.google.common.collect.Maps;
import com.tle.beans.ConfigurationProperty;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.LangUtils;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings({"nls"})
public class MigrateKalturaSettings extends AbstractHibernateDataMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.kaltura.migration.info.settings");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// Get all kaltura settings
		Query query = session.createQuery("FROM ConfigurationProperty WHERE property LIKE 'kaltura.%'");
		List<ConfigurationProperty> configs = query.list();

		Map<Long, Map<String, String>> kalturaSettingsMap = Maps.newHashMap();

		for( ConfigurationProperty cp : configs )
		{
			long instId = cp.getKey().getInstitutionId();

			Map<String, String> keyValues = kalturaSettingsMap.get(instId);
			if( keyValues == null )
			{
				keyValues = new HashMap<String, String>();
			}

			keyValues.put(cp.getKey().getProperty(), cp.getValue());
			kalturaSettingsMap.put(instId, keyValues);
		}

		// Make entities
		for( Entry<Long, Map<String, String>> entry : kalturaSettingsMap.entrySet() )
		{
			Map<String, String> ksMap = entry.getValue();

			KalturaServer ks = new KalturaServer();

			ks.setUuid(UUID.randomUUID().toString());
			ks.setDateCreated(new Date());
			ks.setDateModified(ks.getDateCreated());

			Institution inst = new Institution();
			inst.setDatabaseId(entry.getKey());
			ks.setInstitution(inst);

			ks.setEnabled(Boolean.parseBoolean(ksMap.get("kaltura.enabled")));
			ks.setName(LangUtils.createTextTempLangugageBundle("Kaltura.com SaaS"));
			String endPoint = ksMap.get("kaltura.endpoint");
			ks.setEndPoint(endPoint != null ? endPoint : KalturaUtils.KALTURA_SAAS_ENDPOINT);
			ks.setPartnerId(Integer.parseInt(ksMap.get("kaltura.partnerid")));
			ks.setAdminSecret(ksMap.get("kaltura.adminsecret"));
			ks.setUserSecret(ksMap.get("kaltura.usersecret"));
			ks.setKdpUiConfId(Integer.parseInt(KalturaUtils.KALTURA_SAAS_DEFAULT_PLAYER_ID));

			session.save(ks);
			session.flush();

			result.incrementStatus();
		}

		// Remove kaltura settings
		session.createSQLQuery("DELETE FROM configuration_property WHERE property LIKE 'kaltura.%'").executeUpdate();
		result.incrementStatus();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		SQLQuery query = session
			.createSQLQuery("SELECT count(DISTINCT institution_id) FROM configuration_property WHERE property LIKE 'kaltura%'");
		return count(query);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{ConfigurationProperty.class, KalturaServer.class, BaseEntity.class,
				BaseEntity.Attribute.class, LanguageBundle.class, Institution.class, LanguageString.class};
	}
}
