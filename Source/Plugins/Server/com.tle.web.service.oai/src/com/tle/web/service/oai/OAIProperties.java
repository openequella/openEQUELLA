/*
 * Created on 25/01/2006
 */
package com.tle.web.service.oai;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import com.tle.beans.system.MailSettings;
import com.tle.beans.system.OAISettings;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;

@Bind
public class OAIProperties extends Properties
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ConfigurationService configConstants;
	@Inject
	private UrlService urlService;

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
			return urlService.getInstitutionUrl() + "p/oai"; //$NON-NLS-1$
		}
		return super.getProperty(key);
	}
}
