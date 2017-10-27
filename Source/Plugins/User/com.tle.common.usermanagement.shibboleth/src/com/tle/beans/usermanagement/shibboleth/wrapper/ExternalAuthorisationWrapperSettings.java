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

package com.tle.beans.usermanagement.shibboleth.wrapper;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;

/**
 * @author aholland
 */
public class ExternalAuthorisationWrapperSettings extends UserManagementSettings
{
	public static final String USAGE_REMOTE_USER = "R"; //$NON-NLS-1$
	public static final String USAGE_HTTP_HEADER = "H"; //$NON-NLS-1$
	public static final String USAGE_ENV_VAR = "E"; //$NON-NLS-1$

	private static final long serialVersionUID = -1473172672982262252L;

	@Property(key = "wrapper.shibboleth.useridentifier")
	private String httpHeaderName;
	@Property(key = "wrapper.shibboleth.environmentvarname")
	private String environmentVarName;

	@Property(key = "wrapper.shibboleth.usagetype")
	private String usageType;

	@Property(key = "wrapper.shibboleth.logouturl")
	private String logoutUrl;
	@Property(key = "wrapper.shibboleth.enabled")
	boolean enabled;

	public String getHttpHeaderName()
	{
		return httpHeaderName;
	}

	public void setHttpHeaderName(String httpHeaderName)
	{
		this.httpHeaderName = httpHeaderName;
	}

	public String getEnvironmentVarName()
	{
		return environmentVarName;
	}

	public void setEnvironmentVarName(String environmentVarName)
	{
		this.environmentVarName = environmentVarName;
	}

	public String getUsageType()
	{
		return usageType;
	}

	public void setUsageType(String usageType)
	{
		this.usageType = usageType;
	}

	public String getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl)
	{
		this.logoutUrl = logoutUrl;
	}

	public boolean isRemoteUser()
	{
		return (usageType == null || USAGE_REMOTE_USER.equals(usageType) || usageType.trim().length() == 0);
	}

	public boolean isHTTPHeader()
	{
		return (usageType != null && usageType.equals(USAGE_HTTP_HEADER));
	}

	public boolean isEnvironmentVar()
	{
		return (usageType != null && usageType.equals(USAGE_ENV_VAR));
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
