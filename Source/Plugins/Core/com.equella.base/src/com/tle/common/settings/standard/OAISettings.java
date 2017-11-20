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

package com.tle.common.settings.standard;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

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
