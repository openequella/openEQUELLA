package com.tle.core.echo.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class EchoServer extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private String applicationUrl;
	private String contentUrl;

	private String consumerKey;
	private String consumerSecret;

	private String echoSystemID;

	public EchoServer()
	{
		// For hibernate
	}

	public String getApplicationUrl()
	{
		return applicationUrl;
	}

	public void setApplicationUrl(String applicationUrl)
	{
		this.applicationUrl = applicationUrl;
	}

	public String getContentUrl()
	{
		return contentUrl;
	}

	public void setContentUrl(String contentUrl)
	{
		this.contentUrl = contentUrl;
	}

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}

	public String getEchoSystemID()
	{
		return echoSystemID;
	}

	public void setEchoSystemID(String echoSystemID)
	{
		this.echoSystemID = echoSystemID;
	}
}
