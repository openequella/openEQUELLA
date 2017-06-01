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

import com.tle.core.events.listeners.UMPChangedListener;

/**
 * @author Nicholas Read
 */
public class UMPChangedEvent extends ApplicationEvent<UMPChangedListener>
{
	private static final long serialVersionUID = 1L;

	private final String purgeIdFromCaches;
	private final boolean groupPurge;

	public UMPChangedEvent()
	{
		this(null, false);
	}

	public UMPChangedEvent(String purgeIdFromCaches, boolean groupPurge)
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.purgeIdFromCaches = purgeIdFromCaches;
		this.groupPurge = groupPurge;
	}

	public String getPurgeIdFromCaches()
	{
		return purgeIdFromCaches;
	}

	public boolean isGroupPurge()
	{
		return groupPurge;
	}
	
	@Override
	public Class<UMPChangedListener> getListener()
	{
		return UMPChangedListener.class;
	}

	@Override
	public void postEvent(UMPChangedListener listener)
	{
		listener.umpChangedEvent(this);
	}
}
