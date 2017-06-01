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

package com.tle.core.hibernate.event;

import java.util.Collection;

import com.tle.common.Check;
import com.tle.core.events.ApplicationEvent;

public class SchemaEvent extends ApplicationEvent<SchemaListener>
{
	private static final long serialVersionUID = 1L;

	private Collection<Long> availableSchemas;
	private Collection<Long> unavailableSchemas;
	private boolean systemUp;

	public SchemaEvent(boolean systemUp)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.systemUp = systemUp;
	}

	public SchemaEvent(Collection<Long> availableSchemas, Collection<Long> unavailableSchemas)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.availableSchemas = availableSchemas;
		this.unavailableSchemas = unavailableSchemas;
	}

	@Override
	public void postEvent(SchemaListener listener)
	{
		if( systemUp )
		{
			listener.systemSchemaUp();
		}
		if( !Check.isEmpty(availableSchemas) )
		{
			listener.schemasAvailable(availableSchemas);
		}
		if( !Check.isEmpty(unavailableSchemas) )
		{
			listener.schemasUnavailable(unavailableSchemas);
		}
	}

	@Override
	public Class<SchemaListener> getListener()
	{
		return SchemaListener.class;
	}
}
