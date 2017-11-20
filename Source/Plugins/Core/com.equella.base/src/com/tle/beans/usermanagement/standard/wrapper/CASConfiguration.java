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

package com.tle.beans.usermanagement.standard.wrapper;

import java.net.MalformedURLException;
import java.net.URL;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;

public class CASConfiguration extends UserManagementSettings
{
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_URL = "https://secure.its.yale.edu/cas/servlet/"; //$NON-NLS-1$

	@Property(key = "cas.url")
	private URL url;

	@Property(key = "cas.logout.url")
	private URL logoutUrl;

	@Property(key = "wrapper.cas.enabled")
	private boolean enabled;

	public CASConfiguration()
	{
		try
		{
			setUrl(new URL(DEFAULT_URL));
			setLogoutUrl(new URL(new URL(DEFAULT_URL), "logout?url=")); //$NON-NLS-1$
		}
		catch( MalformedURLException e )
		{
			// NEVER HAPPEN
		}
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

	public URL getUrl()
	{
		return url;
	}

	public void setUrl(URL url)
	{
		this.url = url;
	}

	public URL getLogoutUrl()
	{
		return logoutUrl;
	}

	public void setLogoutUrl(URL logoutUrl)
	{
		this.logoutUrl = logoutUrl;
	}
}