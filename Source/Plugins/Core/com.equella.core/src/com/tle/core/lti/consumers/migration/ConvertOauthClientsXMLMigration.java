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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.thoughtworks.xstream.XStream;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.lti.consumers.LtiConsumerConstants.UnknownUser;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.oauth.service.impl.OAuthServiceImpl;

@Bind
public class ConvertOauthClientsXMLMigration extends XmlMigrator
{

	private static final String OAUTH_ONE_ID = "oog";
	private static final String PROPERTY_LTI_CREATE_USERS = "lti.createusers";
	private static final String PROPERTY_ROLE_INSTRUCTOR = "role.instructor";
	private static final String PROPERTY_ROLE_OTHER = "role.other";

	@Inject
	private OAuthService oauthService;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		final SubTemporaryFile propertiesFolder = new SubTemporaryFile(staging, "properties");
		Map<String, String> allProperties = ((Map<String, String>) xmlHelper.readXmlFile(propertiesFolder,
			"properties.xml"));

		int unknownStrat = UnknownUser.DENY.getValue();
		if( allProperties.containsKey(PROPERTY_LTI_CREATE_USERS)
			&& allProperties.get(PROPERTY_LTI_CREATE_USERS).equals("true") )
		{
			unknownStrat = UnknownUser.CREATE.getValue();
		}

		final SubTemporaryFile oauthfolder = new SubTemporaryFile(staging, "oauthclient");
		final SubTemporaryFile consumersFolder = new SubTemporaryFile(staging, "lticonsumer");

		final List<String> entries = xmlHelper.getXmlFileList(oauthfolder);

		XStream xstream = oauthService.getXStream();

		for( String entry : entries )
		{
			OAuthClient client = (OAuthClient) xmlHelper.readXmlFile(oauthfolder, entry, xstream);
			if( OAUTH_ONE_ID.equals(client.getAttribute(OAuthServiceImpl.KEY_OAUTH_FLOW)) )
			{
				LtiConsumer consumer = newConsumer();
				consumer.setDateCreated(client.getDateCreated());
				consumer.setName(client.getName());
				consumer.setOwner(client.getOwner());
				consumer.setConsumerKey(client.getClientId());
				consumer.setConsumerSecret(client.getClientSecret());
				consumer.setUnknownUser(unknownStrat);
				if( allProperties.containsKey(PROPERTY_ROLE_INSTRUCTOR) )
				{
					consumer.setInstructorRoles(Collections.singleton(allProperties.get(PROPERTY_ROLE_INSTRUCTOR)));
				}
				if( allProperties.containsKey(PROPERTY_ROLE_OTHER) )
				{
					consumer.setOtherRoles(Collections.singleton(allProperties.get(PROPERTY_ROLE_OTHER)));
				}

				// write entity
				if( !fileExists(consumersFolder, "") )
				{
					xmlHelper.writeExportFormatXmlFile(consumersFolder, true);
				}

				final String uuid = consumer.getUuid();
				final BucketFile bucketFolder = new BucketFile(consumersFolder, uuid);
				xmlHelper.writeXmlFile(bucketFolder, uuid + ".xml", consumer);
				fileSystemService.removeFile(oauthfolder, entry);
			}
		}

	}

	private LtiConsumer newConsumer()
	{
		LtiConsumer consumer = new LtiConsumer();
		consumer.setUuid(UUID.randomUUID().toString());
		consumer.setDateModified(new Date());
		consumer.setDisabled(false);
		consumer.setSystemType(false);
		consumer.setAllowedExpression(Recipient.EVERYONE.getPrefix());
		return consumer;

	}
}
