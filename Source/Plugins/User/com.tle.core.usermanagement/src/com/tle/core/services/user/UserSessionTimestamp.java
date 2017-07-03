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

package com.tle.core.services.user;

import java.io.Serializable;
import java.util.Date;

public class UserSessionTimestamp implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final Date created;
	private final Date accessed;

	private final String sessionId;
	private final boolean isGuest;
	private final String username;
	private final String hostAddress;

	public UserSessionTimestamp(String sessionId, boolean isGuest, String username, String hostAddress)
	{
		this(sessionId, isGuest, username, hostAddress, new Date());
	}

	private UserSessionTimestamp(String sessionId, boolean isGuest, String username, String hostAddress, Date created)
	{
		this(sessionId, isGuest, username, hostAddress, created, created);
	}

	private UserSessionTimestamp(String sessionId, boolean isGuest, String username, String hostAddress, Date created,
		Date accessed)
	{
		this.sessionId = sessionId;
		this.isGuest = isGuest;
		this.username = username;
		this.hostAddress = hostAddress;
		this.created = created;
		this.accessed = accessed;
	}

	public Date getAccessed()
	{
		return accessed;
	}

	public UserSessionTimestamp updatedAccessed(Date accessed)
	{
		return new UserSessionTimestamp(sessionId, isGuest, username, hostAddress, created, accessed);
	}

	public Date getCreated()
	{
		return created;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public boolean isGuest()
	{
		return isGuest;
	}

	public String getUsername()
	{
		return username;
	}

	public String getHostAddress()
	{
		return hostAddress;
	}
}
