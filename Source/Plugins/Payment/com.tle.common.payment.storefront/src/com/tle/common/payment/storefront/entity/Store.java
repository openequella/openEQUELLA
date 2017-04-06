package com.tle.common.payment.storefront.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@SuppressWarnings("nls")
@Entity
@AccessType("field")
public class Store extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "STORE";

	public static final String FIELD_CONNECTED_URL = "ConnectedUrl";
	public static final String FIELD_CONNECTED_CLIENT_ID = "ConnectedClientID";

	@Column(length = 500, nullable = false)
	private String storeUrl;

	@Column(length = 100, nullable = false)
	private String clientId;

	@Column(length = 255)
	private String token;

	private Date lastHarvest;

	public String getStoreUrl()
	{
		return storeUrl;
	}

	public void setStoreUrl(String storeUrl)
	{
		this.storeUrl = storeUrl;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
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