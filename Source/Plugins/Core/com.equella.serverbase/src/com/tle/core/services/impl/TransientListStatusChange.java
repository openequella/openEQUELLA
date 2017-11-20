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

package com.tle.core.services.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.core.services.TaskStatusChange;

public class TransientListStatusChange implements TaskStatusChange<TransientListStatusChange>
{
	private static final long serialVersionUID = 1L;
	private Multimap<String, Serializable> changes = ArrayListMultimap.create();
	// Only used during setup, so this property doesn't need to be serialised.
	private final transient String key; // NOSONAR

	public TransientListStatusChange(String key)
	{
		this.key = key;
	}

	@Override
	public void merge(TransientListStatusChange newChanges)
	{
		changes.putAll(newChanges.changes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void modifyStatus(TaskStatusImpl taskStatus)
	{
		Map<String, Object> transMap = taskStatus.getTransientMap();
		Set<String> keys = changes.keySet();
		for( String k : keys )
		{
			List<Object> list = (List<Object>) transMap.get(k);
			if( list == null )
			{
				list = Lists.newArrayList();
				transMap.put(k, list);
			}
			list.addAll(changes.get(k));
		}
	}

	public void add(Serializable entry)
	{
		changes.put(key, entry);
	}

}
