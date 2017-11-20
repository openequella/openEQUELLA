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
