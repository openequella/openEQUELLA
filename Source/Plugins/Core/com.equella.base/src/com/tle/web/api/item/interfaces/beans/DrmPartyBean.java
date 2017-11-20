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

package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class DrmPartyBean
{
	private String userId;
	private String name;
	private String email;
	private boolean owner;

	public DrmPartyBean()
	{
	}

	public DrmPartyBean(String userId, String name, String email, boolean owner)
	{
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.owner = owner;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public boolean isOwner()
	{
		return owner;
	}

	public void setOwner(boolean owner)
	{
		this.owner = owner;
	}
}
