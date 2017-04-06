/*
 * Created on May 25, 2005
 */
package com.tle.beans.system;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

/**
 * @author Nicholas Read
 */
public class OAISettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@Property(key = "oai.scheme")
	private String scheme = "oai"; //$NON-NLS-1$

	@Property(key = "oai.namespace-identifier")
	private String namespaceIdentifier;

	@Property(key = "oai.email")
	private String emailAddress;

	@Property(key = "oai.usedownloaditem")
	private boolean useDownloadItemAcl;

	public boolean isUseDownloadItemAcl()
	{
		return useDownloadItemAcl;
	}

	public void setUseDownloadItemAcl(boolean useDownloadItemAcl)
	{
		this.useDownloadItemAcl = useDownloadItemAcl;
	}

	public void setScheme(String scheme)
	{
		this.scheme = scheme;
	}

	public String getScheme()
	{
		return scheme;
	}

	public void setNamespaceIdentifier(String namespaceIdentifier)
	{
		this.namespaceIdentifier = namespaceIdentifier;
	}

	public String getNamespaceIdentifier()
	{
		return namespaceIdentifier;
	}

	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}
}
