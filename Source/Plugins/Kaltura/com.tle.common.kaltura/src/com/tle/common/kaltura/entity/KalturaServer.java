package com.tle.common.kaltura.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class KalturaServer extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private boolean enabled;

	@Column(length = 1024, nullable = false)
	private String endPoint;
	private int partnerId;
	private int subPartnerId;
	private String adminSecret;
	private String userSecret;
	private int kdpUiConfId;

	public KalturaServer()
	{
		// for hibernate
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getEndPoint()
	{
		return endPoint;
	}

	public void setEndPoint(String endPoint)
	{
		this.endPoint = endPoint;
	}

	public int getPartnerId()
	{
		return partnerId;
	}

	public void setPartnerId(int partnerId)
	{
		this.partnerId = partnerId;
	}

	public int getSubPartnerId()
	{
		return subPartnerId;
	}

	public void setSubPartnerId(int subPartnerId)
	{
		this.subPartnerId = subPartnerId;
	}

	public String getAdminSecret()
	{
		return adminSecret;
	}

	public void setAdminSecret(String adminSecret)
	{
		this.adminSecret = adminSecret;
	}

	public String getUserSecret()
	{
		return userSecret;
	}

	public void setUserSecret(String userSecret)
	{
		this.userSecret = userSecret;
	}

	public int getKdpUiConfId()
	{
		return kdpUiConfId;
	}

	public void setKdpUiConfId(int kdpUiConfId)
	{
		this.kdpUiConfId = kdpUiConfId;
	}
}
