package com.tle.core.oauth;

import java.io.Serializable;

public class OAuthFlowDefinition implements Serializable
{
	private final boolean setUrl;
	private final boolean useInbuiltUrl;
	private final boolean setUser;
	private final String nameKey;
	private final String descriptionKey;
	private final String redirectUrl;
	private final String id;

	public OAuthFlowDefinition(boolean setUrl, boolean useInbuiltUrl, boolean setUser, String nameKey,
		String descriptionKey, String redirectUrl, String id)
	{
		this.setUrl = setUrl;
		this.useInbuiltUrl = useInbuiltUrl;
		this.setUser = setUser;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
		this.redirectUrl = redirectUrl;
		this.id = id;
	}

	public boolean isSetUrl()
	{
		return setUrl;
	}

	public boolean isUseInbuiltUrl()
	{
		return useInbuiltUrl;
	}

	public boolean isSetUser()
	{
		return setUser;
	}

	public String getNameKey()
	{
		return nameKey;
	}

	public String getDescriptionKey()
	{
		return descriptionKey;
	}

	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	public String getId()
	{
		return id;
	}
}
