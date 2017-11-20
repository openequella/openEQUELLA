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

package com.tle.core.schema.event;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.event.BaseEntityReferencesEvent;
import com.tle.core.schema.event.listener.SchemaReferencesListener;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public class SchemaReferencesEvent extends BaseEntityReferencesEvent<Schema, SchemaReferencesListener>
{
	private static final long serialVersionUID = 1L;

	public SchemaReferencesEvent(Schema schema)
	{
		super(schema);
	}

	@Override
	public Class<SchemaReferencesListener> getListener()
	{
		return SchemaReferencesListener.class;
	}

	@Override
	public void postEvent(SchemaReferencesListener listener)
	{
		listener.addSchemaReferencingClasses(entity, referencingClasses);
	}
}
