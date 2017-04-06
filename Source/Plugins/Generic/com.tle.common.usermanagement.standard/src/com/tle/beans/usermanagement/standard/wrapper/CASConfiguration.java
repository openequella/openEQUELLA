package com.tle.beans.usermanagement.standard.wrapper;

import java.net.MalformedURLException;
import java.net.URL;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;

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