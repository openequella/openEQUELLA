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

import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.common.usermanagement.user.UserState;

/**
 * @author Nicholas Read
 */
public class UserSessionLogoutEvent extends ApplicationEvent<UserSessionLogoutListener> implements UserSessionEvent
{
	private static final long serialVersionUID = 1L;
	private final boolean entireHttpSessionDestroyed;
	private final UserState userState;
	private final String sessionId;

	public UserSessionLogoutEvent(UserState userState, boolean entireHttpSessionDestroyed)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);

		this.userState = userState;
		this.entireHttpSessionDestroyed = entireHttpSessionDestroyed;
		this.sessionId = userState.getSessionID();
	}

	public boolean isEntireHttpSessionDestroyed()
	{
		return entireHttpSessionDestroyed;
	}

	@Override
	public UserState getUserState()
	{
		return userState;
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	@Override
	public Class<UserSessionLogoutListener> getListener()
	{
		return UserSessionLogoutListener.class;
	}

	@Override
	public void postEvent(UserSessionLogoutListener listener)
	{
		listener.userSessionDestroyedEvent(this);
	}
}
