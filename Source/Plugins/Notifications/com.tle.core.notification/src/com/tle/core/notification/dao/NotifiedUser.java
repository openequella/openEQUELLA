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

package com.tle.core.notification.dao;

public class NotifiedUser
{
	private final String user;
	private final long instId;

	public NotifiedUser(String user, long instId)
	{
		this.user = user;
		this.instId = instId;
	}

	public String getUser()
	{
		return user;
	}

	public long getInstId()
	{
		return instId;
	}
}
