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

package com.tle.common.externaltools.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.NameValue;

@Entity
@AccessType("field")
public class ExternalTool extends BaseEntity
{
	public static final String ENTITY_TYPE = "EXTERNAL_TOOL";

	@Lob
	private String baseURL;
	@Column(length = 64, nullable = true)
	private String consumerKey;
	@Column(length = 64, nullable = true)
	private String sharedSecret;

	@Type(type = "xstream_immutable")
	@Column(length = 8192)
	private List<NameValue> customParams;
	private Boolean shareName;
	private Boolean shareEmail;

	public List<NameValue> getCustomParams()
	{
		return customParams;
	}

	public void setCustomParams(List<NameValue> customParams)
	{
		this.customParams = customParams;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getSharedSecret()
	{
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret)
	{
		this.sharedSecret = sharedSecret;
	}

	public Boolean getShareName()
	{
		return shareName;
	}

	public void setShareName(Boolean shareName)
	{
		this.shareName = shareName;
	}

	public Boolean getShareEmail()
	{
		return shareEmail;
	}

	public void setShareEmail(Boolean shareEmail)
	{
		this.shareEmail = shareEmail;
	}
}
