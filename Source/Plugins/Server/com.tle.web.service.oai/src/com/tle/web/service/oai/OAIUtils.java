package com.tle.web.service.oai;

import java.util.concurrent.TimeUnit;

import ORG.oclc.oai.server.verb.IdDoesNotExistException;

import com.tle.beans.item.ItemId;
import com.tle.beans.system.OAISettings;
import com.tle.common.Check;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;

public final class OAIUtils
{
	private static long cachedTime = 0;
	private static OAIUtils cachedUtils;

	public static synchronized OAIUtils getInstance(UrlService urlService, ConfigurationService configService)
	{
		final long now = System.currentTimeMillis();
		if( now > cachedTime + TimeUnit.MINUTES.toMillis(1) )
		{
			cachedTime = now;
			cachedUtils = new OAIUtils(urlService, configService.getProperties(new OAISettings()));
		}
		return cachedUtils;
	}

	// // OBJECT INSTANCE STUFF BELOW /////////////////////////////////////////

	private final OAISettings settings;

	// Cached
	private transient String namespaceIdentifier;
	private transient String schemaPlusNamespace;

	private OAIUtils(final UrlService urlService, final OAISettings settings)
	{
		this.settings = settings;

		namespaceIdentifier = settings.getNamespaceIdentifier();
		if( Check.isEmpty(namespaceIdentifier) )
		{
			namespaceIdentifier = urlService.getInstitutionUrl().getHost();
		}

		schemaPlusNamespace = settings.getScheme() + ':' + namespaceIdentifier + ':';
	}

	public String getScheme()
	{
		return settings.getScheme();
	}

	public String getNamespaceIdentifier()
	{
		return namespaceIdentifier;
	}

	public String getIdentifier(final ItemId itemId)
	{
		return schemaPlusNamespace + itemId.toString();
	}

	public String getSampleIdentifier()
	{
		return getIdentifier(new ItemId("ABCDEF", 1)); //$NON-NLS-1$
	}

	public boolean isUseDownloadItemAcl()
	{
		return settings.isUseDownloadItemAcl();
	}

	public ItemId parseRecordIdentifier(final String id) throws IdDoesNotExistException
	{
		if( !id.startsWith(schemaPlusNamespace) )
		{
			throw new IdDoesNotExistException(id);
		}
		else
		{
			return new ItemId(id.substring(schemaPlusNamespace.length()));
		}
	}
}
