package com.tle.core.kaltura.migration;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import com.tle.common.i18n.LangUtils;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class MigrateKalturaSettingsXml extends XmlMigrator
{
	private static final String KALTURA_USERSECRET = "kaltura.usersecret";
	private static final String KALTURA_ADMINSECRET = "kaltura.adminsecret";
	private static final String KALTURA_ENABLED = "kaltura.enabled";
	private static final String KALTURA_PARTNERID = "kaltura.partnerid";
	private static final String PROPERTIES_PATH = "properties/properties.xml";

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		if( !fileExists(staging, PROPERTIES_PATH) )
		{
			return;
		}

		// Get system settings
		Map<String, String> sysProps = (Map<String, String>) xmlHelper.readXmlFile(staging, PROPERTIES_PATH);

		KalturaServer ks = new KalturaServer();
		if( hasKalturaSettings(sysProps) )
		{
			ks.setUuid(UUID.randomUUID().toString());
			ks.setName(LangUtils.createTextTempLangugageBundle("Kaltura.com SaaS"));
			ks.setDateCreated(new Date());
			ks.setDateModified(ks.getDateCreated());

			ks.setEnabled(Boolean.parseBoolean(sysProps.get(KALTURA_ENABLED)));
			ks.setEndPoint(KalturaUtils.KALTURA_SAAS_ENDPOINT);
			ks.setPartnerId(Integer.parseInt(sysProps.get(KALTURA_PARTNERID)));
			ks.setAdminSecret(sysProps.get(KALTURA_ADMINSECRET));
			ks.setUserSecret(sysProps.get(KALTURA_USERSECRET));
			ks.setKdpUiConfId(Integer.parseInt(KalturaUtils.KALTURA_SAAS_DEFAULT_PLAYER_ID));

			// Write kaltura entity
			final SubTemporaryFile exportFolder = new SubTemporaryFile(staging, "kalturaserver");
			xmlHelper.writeExportFormatXmlFile(exportFolder, true);

			final EntityFile entityFile = new EntityFile(ks);
			final String uuid = ks.getUuid();
			final BucketFile bucketFolder = new BucketFile(exportFolder, uuid);
			final SubTemporaryFile entityExportFolder = new SubTemporaryFile(bucketFolder, uuid);

			fileSystemService.copyToStaging(entityFile, entityExportFolder, false);
			ks.setInstitution(null);
			xmlHelper.writeXmlFile(bucketFolder, uuid + ".xml", ks);
		}

		// Remove Kaltura system settings
		sysProps.remove(KALTURA_ENABLED);
		sysProps.remove(KALTURA_PARTNERID);
		sysProps.remove(KALTURA_ADMINSECRET);
		sysProps.remove(KALTURA_USERSECRET);

		// Write system settings
		xmlHelper.writeXmlFile(staging, PROPERTIES_PATH, sysProps);
	}

	private boolean hasKalturaSettings(Map<String, String> m)
	{
		return m.containsKey(KALTURA_ENABLED) && m.containsKey(KALTURA_PARTNERID) && m.containsKey(KALTURA_ADMINSECRET)
			&& m.containsKey(KALTURA_USERSECRET);
	}
}
