package com.tle.core.oauth.migration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.oauth.service.OAuthService;

/**
 * @author Aaron
 *
 */
@Bind
@Singleton
public class OAuthSecurityXmlMigration extends XmlMigrator
{
	@Inject
	private EncryptionService encryptionService;
	@Inject
	private OAuthService oauthService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		// OAuth Client secrets
		final SubTemporaryFile oauthfolder = new SubTemporaryFile(staging, "oauthclient");
		final List<String> oauthentries = xmlHelper.getXmlFileList(oauthfolder);
		for( String entry : oauthentries )
		{
			OAuthClient client = (OAuthClient) xmlHelper.readXmlFile(oauthfolder, entry, oauthService.getXStream());
			String encpwd = encryptionService.encrypt(client.getClientSecret());
			client.setClientSecret(encpwd);
			xmlHelper.writeXmlFile(oauthfolder, entry, client);
		}
	}
}
