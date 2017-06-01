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

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.events.listeners.ItemDefinitionReferencesListener;

/**
 * @author Aaron
 */
public class ItemDefinitionReferencesEvent
	extends
		BaseEntityReferencesEvent<ItemDefinition, ItemDefinitionReferencesListener>
{
	public ItemDefinitionReferencesEvent(ItemDefinition entity)
	{
		super(entity);
	}

	private final List<Class<?>> referencingClasses = Lists.newArrayList();

	@Override
	public Class<ItemDefinitionReferencesListener> getListener()
	{
		return ItemDefinitionReferencesListener.class;
	}

	@Override
	public void postEvent(ItemDefinitionReferencesListener listener)
	{
		listener.addItemDefinitionReferencingClasses(entity, referencingClasses);
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
