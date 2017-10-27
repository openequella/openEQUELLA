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

package com.tle.core.institution.events;

import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;

/**
 * @author Nicholas Read
 */
public class InstitutionEvent extends ApplicationEvent<InstitutionListener>
{
	public enum InstitutionEventType
	{
		AVAILABLE, UNAVAILABLE, DELETED, EDITED, STATUS
	}

	private static final long serialVersionUID = 1L;
	private Multimap<Long, Institution> changes;
	private final InstitutionEventType eventType;

	public InstitutionEvent(InstitutionEventType eventType, Multimap<Long, Institution> changes)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.eventType = eventType;
		this.changes = changes;
	}

	@Override
	public Class<InstitutionListener> getListener()
	{
		return InstitutionListener.class;
	}

	@Override
	public void postEvent(InstitutionListener listener)
	{
		listener.institutionEvent(this);
	}

	/**
	 * @return A map of database schema IDs to modified institutions
	 */
	public Multimap<Long, Institution> getChanges()
	{
		return changes;
	}

	public void setChanges(Multimap<Long, Institution> changes)
	{
		this.changes = changes;
	}

	public InstitutionEventType getEventType()
	{
		return eventType;
	}
}
