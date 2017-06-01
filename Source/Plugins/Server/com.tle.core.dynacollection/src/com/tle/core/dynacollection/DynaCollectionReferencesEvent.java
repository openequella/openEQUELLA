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

package com.tle.core.dynacollection;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.entity.DynaCollection;
import com.tle.core.events.BaseEntityReferencesEvent;

public class DynaCollectionReferencesEvent
	extends
		BaseEntityReferencesEvent<DynaCollection, DynaCollectionReferencesListener>
{
	private static final long serialVersionUID = 1L;

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	public DynaCollectionReferencesEvent(DynaCollection dc)
	{
		super(dc);
	}

	@Override
	public Class<DynaCollectionReferencesListener> getListener()
	{
		return DynaCollectionReferencesListener.class;
	}

	@Override
	public void postEvent(DynaCollectionReferencesListener listener)
	{
		listener.addDynaCollectionReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
