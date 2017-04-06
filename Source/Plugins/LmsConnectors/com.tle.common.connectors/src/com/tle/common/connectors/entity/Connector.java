package com.tle.common.connectors.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
public final class Connector extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 16, nullable = false)
	private String lmsType;
	@Column(length = 1024, nullable = false)
	private String serverUrl;
	private boolean useLoggedInUsername;
	@Lob
	private String usernameScript;

	@Transient
	private Object extraData;

	public Connector()
	{
		// for hibernate
	}

	public Connector(String lmsType)
	{
		this.lmsType = lmsType;
	}

	public String getLmsType()
	{
		return lmsType;
	}

	public void setLmsType(String lmsType)
	{
		this.lmsType = lmsType;
	}

	public String getServerUrl()
	{
		return serverUrl;
	}

	public void setServerUrl(String serverUrl)
	{
		this.serverUrl = serverUrl;
	}

	public boolean isUseLoggedInUsername()
	{
		return useLoggedInUsername;
	}

	public void setUseLoggedInUsername(boolean useLoggedInUsername)
	{
		this.useLoggedInUsername = useLoggedInUsername;
	}

	public String getUsernameScript()
	{
		return usernameScript;
	}

	public void setUsernameScript(String usernameScript)
	{
		this.usernameScript = usernameScript;
	}

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}
}
