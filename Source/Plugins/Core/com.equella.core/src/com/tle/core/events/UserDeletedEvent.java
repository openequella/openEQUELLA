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

package com.tle.core.events;

import com.google.common.base.Preconditions;
import com.tle.core.events.listeners.UserChangeListener;

/**
 * @author Nicholas Read
 */
public class UserDeletedEvent extends ApplicationEvent<UserChangeListener>
{
	private static final long serialVersionUID = 1L;

	private final String userID;

	public UserDeletedEvent(String userID, boolean synchronous)
	{
		super(synchronous ? PostTo.POST_TO_SELF_SYNCHRONOUSLY : PostTo.POST_ONLY_TO_SELF);

		Preconditions.checkNotNull(userID);
		this.userID = userID;
	}

	public UserDeletedEvent(String userID)
	{
		this(userID, false);
	}

	public String getUserID()
	{
		return userID;
	}

	@Override
	public Class<UserChangeListener> getListener()
	{
		return UserChangeListener.class;
	}

	@Override
	public void postEvent(UserChangeListener listener)
	{
		listener.userDeletedEvent(this);
	}
}
