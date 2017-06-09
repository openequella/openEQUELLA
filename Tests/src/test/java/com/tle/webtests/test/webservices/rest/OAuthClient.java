package com.tle.webtests.test.webservices.rest;

public class OAuthClient
{
	private String secret;
	private String clientId;
	private String name;
	private String url;
	private String username;
	private boolean defaultRedirect;

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public boolean isDefaultRedirect()
	{
		return defaultRedirect;
	}
	public void setDefaultRedirect(boolean defaultRedirect)
	{
		this.defaultRedirect = defaultRedirect;
	}
	public String getSecret()
	{
		return secret;
	}
	public void setSecret(String secret)
	{
		this.secret = secret;
	}
	public String getClientId()
	{
		return clientId;
	}
	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}
}