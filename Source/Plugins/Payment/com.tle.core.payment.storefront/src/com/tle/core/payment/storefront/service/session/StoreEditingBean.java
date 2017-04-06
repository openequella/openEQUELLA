package com.tle.core.payment.storefront.service.session;

import java.util.Date;

import com.tle.core.services.entity.EntityEditingBean;

public class StoreEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String storeUrl;
	private String clientId;
	private String token;
	private String icon;
	private Date lastHarvest;

	public void setStoreUrl(String storeUrl)
	{
		this.storeUrl = storeUrl;
	}

	public String getStoreUrl()
	{
		return storeUrl;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getClientId()
	{
		return clientId;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getIcon()
	{
		return icon;
	}

	public Date getLastHarvest()
	{
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest)
	{
		this.lastHarvest = lastHarvest;
	}
}