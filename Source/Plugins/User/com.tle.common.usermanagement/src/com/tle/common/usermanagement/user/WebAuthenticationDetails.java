package com.tle.common.usermanagement.user;

import java.io.Serializable;

/**
 * @author Nicholas Read
 */
public class WebAuthenticationDetails implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final String referrer;
	private final String ipAddress;
	private final String hostAddress;

	public WebAuthenticationDetails(UserState userState)
	{
		this(userState.getHostReferrer(), userState.getIpAddress(), userState.getHostAddress());
	}

	public WebAuthenticationDetails(String referrer, String ipAddress, String hostAddress)
	{
		this.referrer = referrer;
		this.ipAddress = ipAddress;
		this.hostAddress = hostAddress;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public String getReferrer()
	{
		return referrer;
	}

	public String getHostAddress()
	{
		return hostAddress;
	}
}
