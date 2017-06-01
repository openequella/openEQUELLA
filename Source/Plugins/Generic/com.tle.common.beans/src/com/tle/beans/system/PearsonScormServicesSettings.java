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

package com.tle.beans.system;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

public class PearsonScormServicesSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = -6453184772230531991L;

	@Property(key = "scorm.pss.enable")
	private boolean enable;

	@Property(key = "scorm.pss.namespace")
	private String accountNamespace;

	@Property(key = "scorm.pss.consumerkey")
	private String consumerKey;

	@Property(key = "scorm.pss.consumersecret")
	private String consumerSecret;

	@Property(key = "scorm.pss.baseurl")
	private String baseUrl;

	public boolean isEnable()
	{
		return enable;
	}

	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}

	public String getAccountNamespace()
	{
		return accountNamespace;
	}

	public void setAccountNamespace(String accountNamespace)
	{
		this.accountNamespace = accountNamespace;
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

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}
}
