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

package com.dytech.edge.common;

import com.dytech.edge.exceptions.WorkflowException;

/**
 * This exception indicates that a resource has been locked by some entity.
 * 
 * @author Nicholas Read
 */
public class LockedException extends WorkflowException
{
	private static final long serialVersionUID = 1L;

	private final String userID;
	private final String sessionID;
	private final long entityId;

	public LockedException(String msg, String userID, String sessionID, long entityId)
	{
		super(msg);
		this.userID = userID;
		this.sessionID = sessionID;
		this.entityId = entityId;
	}

	public String getUserID()
	{
		return userID;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}

	@Override
	public boolean isSilent()
	{
		return false;
	}

	@Override
	public boolean isWarnOnly()
	{
		return true;
	}

	public long getEntityId()
	{
		return entityId;
	}
}
