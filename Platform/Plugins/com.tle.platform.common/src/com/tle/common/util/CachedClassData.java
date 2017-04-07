package com.tle.common.util;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class CachedClassData<T>
{
	private Map<Class<?>, T> map = new WeakHashMap<Class<?>, T>();

	public synchronized T getForClass(Class<?> clazz)
	{
		T scanner = map.get(clazz);
		if( scanner == null && !map.containsKey(clazz) )
		{
			scanner = newEntry(clazz);
			map.put(clazz, scanner);
		}
		return scanner;
	}

	protected abstract T newEntry(Class<?> clazz);
}
