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

package com.tle.core.externaltools.beans;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class ExternalToolAttachmentBean extends EquellaAttachmentBean
{
	private String externalToolProviderUuid;
	private String launchUrl;
	private String customParameters;
	private String consumerKey;
	private String consumerSecret;
	private String iconUrl;
	private boolean shareUserNameDetails;
	private boolean shareUserEmailDetails;

	@Override
	public String getRawAttachmentType()
	{
		return "custom/lti";
	}

	public String getExternalToolProviderUuid()
	{
		return externalToolProviderUuid;
	}

	public void setExternalToolProviderUuid(String externalToolProviderUuid)
	{
		this.externalToolProviderUuid = externalToolProviderUuid;
	}

	public String getLaunchUrl()
	{
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl)
	{
		this.launchUrl = launchUrl;
	}

	public String getCustomParameters()
	{
		return customParameters;
	}

	public void setCustomParameters(String customParameters)
	{
		this.customParameters = customParameters;
	}

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

	public boolean isShareUserNameDetails()
	{
		return shareUserNameDetails;
	}

	public void setShareUserNameDetails(boolean shareUserNameDetails)
	{
		this.shareUserNameDetails = shareUserNameDetails;
	}

	public boolean isShareUserEmailDetails()
	{
		return shareUserEmailDetails;
	}

	public void setShareUserEmailDetails(boolean shareUserEmailDetails)
	{
		this.shareUserEmailDetails = shareUserEmailDetails;
	}
}