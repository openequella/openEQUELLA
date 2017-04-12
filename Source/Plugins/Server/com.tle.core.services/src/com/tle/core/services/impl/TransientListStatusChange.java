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
