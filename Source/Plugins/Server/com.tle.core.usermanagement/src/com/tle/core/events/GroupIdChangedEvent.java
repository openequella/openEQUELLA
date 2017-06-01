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

import com.tle.core.events.listeners.GroupChangedListener;

/**
 * @author Nicholas Read
 */
public class GroupIdChangedEvent extends ApplicationEvent<GroupChangedListener>
{
	private static final long serialVersionUID = 1L;

	private final String fromGroupId;
	private final String toGroupId;

	public GroupIdChangedEvent(String fromGroupId, String toGroupId)
	{
		super(PostTo.POST_ONLY_TO_SELF);
		this.fromGroupId = fromGroupId;
		this.toGroupId = toGroupId;
	}

	public String getFromGroupId()
	{
		return fromGroupId;
	}

	public String getToGroupId()
	{
		return toGroupId;
	}

	@Override
	public Class<GroupChangedListener> getListener()
	{
		return GroupChangedListener.class;
	}

	@Override
	public void postEvent(GroupChangedListener listener)
	{
		listener.groupIdChangedEvent(this);
	}
}
