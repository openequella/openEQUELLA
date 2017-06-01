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

package com.tle.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class Staging
{
	@Id
	@Column(length = 40)
	private String stagingID;
	@Column(length = 40)
	private String userSession;

	public Staging()
	{
		super();
	}

	public String getStagingID()
	{
		return stagingID;
	}

	public void setStagingID(String stagingID)
	{
		this.stagingID = stagingID;
	}

	public String getUserSession()
	{
		return userSession;
	}

	public void setUserSession(String userSession)
	{
		this.userSession = userSession;
	}
}
