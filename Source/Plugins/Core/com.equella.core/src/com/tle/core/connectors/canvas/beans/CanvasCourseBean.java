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

package com.tle.core.connectors.canvas.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
public class CanvasCourseBean
{
	private String id;
	private String name;
	@JsonProperty("course_code")
	private String code;
	@JsonProperty("workflow_state")
	private String state;
	@JsonProperty("root_account_id")
	private String rootAccountId;
	@JsonProperty("account_id")
	private String accountId;

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getAccountId()
	{
		return accountId;
	}

	public void setAccountId(String accountId)
	{
		this.accountId = accountId;
	}

	public String getRootAccountId()
	{
		return rootAccountId;
	}

	public void setRootAccountId(String rootAccountId)
	{
		this.rootAccountId = rootAccountId;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean equals = super.equals(obj);
		if( obj instanceof CanvasCourseBean )
		{
			equals |= ((CanvasCourseBean) obj).getId().equals(this.getId());
		}
		return equals;
	}
}
