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
