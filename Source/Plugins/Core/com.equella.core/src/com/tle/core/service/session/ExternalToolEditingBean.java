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

package com.tle.core.service.session;

import java.util.List;

import com.tle.common.NameValue;
import com.tle.core.entity.EntityEditingBean;

public class ExternalToolEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = -6900063797005595228L;

	private String baseURL;
	private String consumerKey;
	private String sharedSecret;
	private List<NameValue> customParams;
	private boolean shareName;
	private boolean shareEmail;

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getSharedSecret()
	{
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret)
	{
		this.sharedSecret = sharedSecret;
	}

	public boolean isShareName()
	{
		return shareName;
	}

	public void setShareName(boolean shareName)
	{
		this.shareName = shareName;
	}

	public boolean isShareEmail()
	{
		return shareEmail;
	}

	public void setShareEmail(boolean shareEmail)
	{
		this.shareEmail = shareEmail;
	}

	public void setCustomParams(List<NameValue> customParams)
	{
		this.customParams = customParams;
	}

	public List<NameValue> getCustomParams()
	{
		return customParams;
	}
}

