/*
 * Created on 25/01/2006
 */
package com.tle.web.service.oai.legacy;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import ORG.oclc.oai.server.catalog.AbstractCatalog;

import com.google.common.base.Throwables;
import com.tle.beans.system.MailSettings;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;

@Deprecated
@Bind
public class OAIProperties
{
	private final Properties properties;

	@Inject
	private ConfigurationService configConstants;
	@Inject
	private UrlService urlService;

	// Sonar objects to 'throws Throwable' but here we're bound by the
	// declaration in external jar
	@Inject
	public OAIProperties(@Named("oaiLegacyProps") Properties properties) throws Throwable // NOSONAR
	{
		this.properties = new OAIExtendedProperties(properties);
		try
		{
			AbstractCatalog.factory(this.properties, null);
		}
		catch( Exception e )
		{
			Throwables.propagate(e);
		}

	}

	// Unfortunately we can't extend OAIProperties from Properties
	// or otherwise spring autowire will go 'spack'
	private class OAIExtendedProperties extends Properties
	{
		private static final long serialVersionUID = 1L;

		public OAIExtendedProperties(Properties p)
		{
			super(p);
		}

		@Override
		public synchronized String getProperty(String key)
		{
			if( "Identify.adminEmail".equals(key) ) //$NON-NLS-1$
			{
				return Utils.ent(configConstants.getProperties(new MailSettings()).getSender());
			}
			else if( "OAIHandler.baseURL".equals(key) ) //$NON-NLS-1$
			{
				return urlService.getInstitutionUrl() + "oai"; //$NON-NLS-1$
			}
			return super.getProperty(key);
		}
	}

	public Properties getProperties()
	{
		return properties;
	}
}
