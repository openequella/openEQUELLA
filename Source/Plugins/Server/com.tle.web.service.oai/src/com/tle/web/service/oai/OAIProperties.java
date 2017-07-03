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

package com.tle.web.service.oai;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.settings.standard.MailSettings;
import com.tle.common.settings.standard.OAISettings;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.settings.service.ConfigurationService;

@Bind
public class OAIProperties extends Properties
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ConfigurationService configConstants;
	@Inject
	private InstitutionService institutionService;

	@Inject
	public OAIProperties(@Named("oaiProps") Properties properties)
	{
		super(properties);
	}

	@Override
	public synchronized String getProperty(String key)
	{
		if( "Identify.adminEmail".equals(key) ) //$NON-NLS-1$
		{
			String email = configConstants.getProperties(new OAISettings()).getEmailAddress();
			if( Check.isEmpty(email) )
			{
				email = configConstants.getProperties(new MailSettings()).getSender();
			}
			return Utils.ent(email);
		}
		else if( "OAIHandler.baseURL".equals(key) ) //$NON-NLS-1$
		{
			return institutionService.getInstitutionUrl() + "p/oai"; //$NON-NLS-1$
		}
		return super.getProperty(key);
	}
}
