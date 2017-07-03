package com.tle.core.security.convert.migration.v65;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class SecurityXmlMigration extends XmlMigrator
{
	private static final List<String> PWD_KEYS = Lists.newArrayList("mail.password", "ldap.admin.password");
	private static final String PROPERTIES_PATH = "properties/properties.xml";

	@Inject
	private EncryptionService encryptionService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		if( !fileExists(staging, PROPERTIES_PATH) )
		{
			return;
		}

		// Get system settings
		Map<String, String> sysProps = (Map<String, String>) xmlHelper.readXmlFile(staging, PROPERTIES_PATH);

		for( String key : PWD_KEYS )
		{
			if( sysProps.containsKey(key) )
			{
				String encPwd = encryptionService.encrypt(sysProps.get(key));
				sysProps.put(key, encPwd);
			}
		}

		// Write system settings
		xmlHelper.writeXmlFile(staging, PROPERTIES_PATH, sysProps);
	}
}
