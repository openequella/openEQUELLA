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

import com.tle.beans.entity.Schema;
import com.tle.core.entity.event.BaseEntityDeletionEvent;
import com.tle.core.schema.event.listener.SchemaDeletionListener;

/**
 * @author Nicholas Read
 */
public class SchemaDeletionEvent extends BaseEntityDeletionEvent<Schema, SchemaDeletionListener>
{
	public SchemaDeletionEvent(Schema schema)
	{
		super(schema);
	}

	@Override
	public Class<SchemaDeletionListener> getListener()
	{
		return SchemaDeletionListener.class;
	}

	@Override
	public void postEvent(SchemaDeletionListener listener)
	{
		listener.removeReferences(entity);
	}
}
